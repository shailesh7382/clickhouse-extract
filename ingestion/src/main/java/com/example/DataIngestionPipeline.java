package com.example;

import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.metrics.ServerMetrics;
import com.clickhouse.client.api.query.QueryResponse;
import com.example.marketdata.MarketDataListener;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DataIngestionPipeline {

    private static final Logger log = LoggerFactory.getLogger(DataIngestionPipeline.class);
    private static final int MAX_SQL_LENGTH = 10000; // Define the maximum SQL length

    public static void main(String[] args) {
        String queuePath = System.getenv("QUEUE_PATH");
        if (queuePath == null) {
            queuePath = "path/to/queue";
        }

        // Set up Chronicle Queue
        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(queuePath).build()) {
            ExcerptTailer tailer = queue.createTailer();

            // Set up ClickHouse Client
            String endpoint = System.getProperty("CLICKHOUSE_ENDPOINT", "http://localhost:8123");
            String user = System.getProperty("CLICKHOUSE_USER", "default");
            String password = System.getProperty("CLICKHOUSE_PASSWORD", "password123");
            String database = System.getProperty("CLICKHOUSE_DATABASE", "default");

            Client client = new Client.Builder()
                    .addEndpoint(endpoint)
                    .setUsername(user)
                    .setPassword(password)
                    .compressServerResponse(true)
                    .setDefaultDatabase(database)
                    .compressClientRequest(true)
                    .setMaxConnections(10)
                    .enableConnectionPool(true)
                    .useAsyncRequests(true)
                    .setConnectionRequestTimeout(30000, ChronoUnit.SECONDS)
                    .build();

            // Create table if it does not exist
            createTableIfNotExists(client);

            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("INSERT INTO market_data (event_time, service_id, event_id, ccy_pair, bid_prices, ask_prices, volumes, bid_points, ask_points, quote_req_id, quote_id, lp_name, status, tenor) VALUES ");
            int counter = 0;
            while ( tailer.methodReader((MarketDataListener) marketData -> {
                if (marketData != null) {
                    log.info("md={}", marketData);
                    sqlBuilder.append(String.format(
                            "(%d, '%s', '%s', '%s', %s, %s, %s, %s, %s, '%s', '%s', '%s', '%s', '%s'),",
                            marketData.eventTime(),
                            marketData.serviceId(),
                            marketData.eventId(),
                            marketData.getCcyPair(),
                            marketData.getBidPrices().toString(),
                            marketData.getAskPrices().toString(),
                            marketData.getVolumes().toString(),
                            marketData.getBidPoints().toString(),
                            marketData.getAskPoints().toString(),
                            marketData.getQuoteReqId(),
                            marketData.getQuoteId(),
                            marketData.getLpName(),
                            marketData.getStatus(),
                            marketData.getTenor()));
                }
            }).readOne()) {
                // nothing to do
                counter++;
                if (sqlBuilder.length() >= MAX_SQL_LENGTH) {
                    sqlBuilder.setLength(sqlBuilder.length() - 1); // Remove the last comma
                    executeBatch(client, sqlBuilder.toString());
                    sqlBuilder.setLength(0);
                    sqlBuilder.append("INSERT INTO market_data (event_time, service_id, event_id, ccy_pair, bid_prices, ask_prices, volumes, bid_points, ask_points, quote_req_id, quote_id, lp_name, status, tenor) VALUES ");
                    log.info("Processed a batch of market data. Waiting for the next batch... ");
                }
            }

                // Execute any remaining batch
                if (sqlBuilder.length() > 0) {
                    sqlBuilder.setLength(sqlBuilder.length() - 1); // Remove the last comma
                    executeBatch(client, sqlBuilder.toString());
                    log.info("Finished processing all market data. TOTAL: {}", counter);
                }
        } catch (Exception e) {
            log.error("Failed to set up Chronicle Queue", e);
        }
    }


    private static void createTableIfNotExists(Client client) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS market_data (" +
                "event_time DateTime64(9), " +
                "service_id String, " +
                "event_id String, " +
                "ccy_pair String, " +
                "bid_prices Array(Float64), " +
                "ask_prices Array(Float64), " +
                "volumes Array(Float64), " +
                "bid_points Array(Float64), " +
                "ask_points Array(Float64), " +
                "quote_req_id String, " +
                "quote_id String, " +
                "lp_name String, " +
                "status String, " +
                "tenor String" +
                ") ENGINE = MergeTree() " +
                "ORDER BY event_time";

        try (QueryResponse response = client.query(createTableSQL).get(3, TimeUnit.SECONDS)) {
            log.info("Table `market_data` is ready. read rows = {}", response.getReadRows());
        } catch (Exception e) {
            log.error("Failed to create table `market_data`", e);
        }
    }

    private static void executeBatch(Client client, final String batchSql) {
        CompletableFuture.runAsync(() -> {
            try (QueryResponse response = client.query(batchSql).get(10, TimeUnit.SECONDS)) {
                log.info("Batch inserted successfully.rows = {} , BATCH = {}. , rows written = {} ",batchSql.length(), batches, response.getMetrics().getMetric(ServerMetrics.NUM_ROWS_WRITTEN).getLong());
            } catch (Exception e) {
                log.error("Failed to execute batch", e);
            }
        });
        batches ++;
    }

    static int batches = 0;
}