package experiment.clickhouse.service;

import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.data_formats.ClickHouseBinaryFormatReader;
import com.clickhouse.client.api.metrics.ServerMetrics;
import com.clickhouse.client.api.query.QueryResponse;
import experiment.clickhouse.service.fix.LpPriceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class Stream2DbWriter {

    private static final Logger log = LoggerFactory.getLogger(Stream2DbWriter.class);
    private static final int BATCH_SIZE = 100; // Define the batch size

    private final Client client;
    private final List<LpPriceEvent> eventBatch;

    public Stream2DbWriter(@Value("${clickhouse.endpoint}") String endpoint,
                           @Value("${clickhouse.user}") String user,
                           @Value("${clickhouse.password}") String password,
                           @Value("${clickhouse.database}") String database) {
        this.client = new Client.Builder()
                .addEndpoint(endpoint)
                .setUsername(user)
                .setPassword(password)
                .compressServerResponse(true)
                .setDefaultDatabase(database)
                .compressClientRequest(true)
                .setMaxConnections(10)
                .enableConnectionPool(true)
                .useAsyncRequests(false)
                .setConnectionRequestTimeout(30000, ChronoUnit.SECONDS)
                .build();
        this.eventBatch = new ArrayList<>();
    }

    public boolean isServerAlive() {
        log.info("Pinging ClickHouse server to check if it is alive");
        return client.ping();
    }

    public synchronized void insertLpPriceEvent(LpPriceEvent event) {
        eventBatch.add(event);
        if (eventBatch.size() >= BATCH_SIZE) {
            flush();
        }
    }

    private synchronized void flush() {
//        createTableIfNotExists();
        if (eventBatch.isEmpty()) {
            return;
        }

        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO lp_price_events (timestamp, uuid, bidPrices, askPrices, quantities, ccyPair, tenor, localDate, lpName) VALUES ");
        for (LpPriceEvent event : eventBatch) {
            long epochMillis = event.getTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli();
            sqlBuilder.append(String.format(
                    "('%s', '%s', [%s], [%s], [%s], '%s', '%s', '%s', '%s'),",
                    epochMillis,
                    event.getUuid(),
                    arrayToString(event.getBidPrices()),
                    arrayToString(event.getAskPrices()),
                    arrayToString(event.getQuantities()),
                    event.getCcyPair(),
                    event.getTenor(),
                    event.getLocalDate(),
                    event.getLpName()
            ));
        }

        // Remove the last comma
        sqlBuilder.setLength(sqlBuilder.length() - 1);


       // Default format is RowBinaryWithNamesAndTypesFormatReader so reader have all information about columns
        try (QueryResponse response = client.query(sqlBuilder.toString()).get(3, TimeUnit.SECONDS);) {

            // Create a reader to access the data in a convenient way
            ClickHouseBinaryFormatReader reader = client.newBinaryFormatReader(response);

            while (reader.hasNext()) {
                reader.next(); // Read the next record from stream and parse it
                // collecting data
            }
        } catch (Exception e) {
            log.error("Failed to read data", e);
        }

        log.info("Batch of LpPriceEvents inserted successfully {} ", sqlBuilder.toString());
        eventBatch.clear();

    }

    private String arrayToString(double[] array) {
        return Arrays.stream(array)
                .mapToObj(Double::toString)
                .collect(Collectors.joining(", "));
    }

    private void createTableIfNotExists() {
        try {
            // Check if the table exists
            String checkTableSQL = "EXISTS TABLE lp_price_events";
            QueryResponse queryResponse = client.query(checkTableSQL).get(3, TimeUnit.SECONDS);
            long metrics = queryResponse.getMetrics().getMetric(ServerMetrics.RESULT_ROWS).getLong();

            log.info("Metrics {} ", metrics);
            boolean tableExists = metrics > 0L;

            if (!tableExists) {
                // Create the table if it does not exist
                String createTableSQL = "CREATE TABLE lp_price_events (" +
                        "timestamp DateTime64(3), " +
                        "uuid String, " +
                        "bidPrices Array(Float64), " +
                        "askPrices Array(Float64), " +
                        "quantities Array(Float64), " +
                        "ccyPair String, " +
                        "tenor String, " +
                        "localDate Date, " +
                        "lpName String" +
                        ") ENGINE = MergeTree() " +
                        "PARTITION BY toYYYYMM(localDate) " +
                        "ORDER BY timestamp " +
                        "TTL localDate + INTERVAL 1 YEAR";

                client.query(createTableSQL).get(3, TimeUnit.SECONDS);
                log.info("Table created successfully");
            } else {
                log.info("Table already exists");
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Failed to check or create table", e);
            throw new RuntimeException("Failed to check or create table", e);
        } catch (Exception e) {
            log.error("Unexpected error occurred while checking or creating table", e);
            throw new RuntimeException("Unexpected error occurred while checking or creating table", e);
        }
    }
}