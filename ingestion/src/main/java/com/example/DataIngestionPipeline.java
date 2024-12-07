package com.example;

import com.clickhouse.client.ClickHouseNode;
import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.query.QueryResponse;
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
    private static final int BATCH_SIZE = 100; // Define the batch size
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

            StringBuilder sqlBuilder = new StringBuilder(
                    "INSERT INTO market_data (event_time, service_id, event_id, ccy_pair, bid_prices, ask_prices, volumes, bid_points, ask_points, quote_req_id, quote_id, lp_name, status, tenor) VALUES ");

            while ( tailer.methodReader(new MarketDataListener() {
                    @Override
                    public void marketData(MarketData marketData) {
                        if (marketData != null) {
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

                            if (sqlBuilder.length() >= MAX_SQL_LENGTH) {
                                executeBatch(client, sqlBuilder);
                                sqlBuilder.setLength(0);
                                sqlBuilder.append("INSERT INTO market_data (event_time, service_id, event_id, ccy_pair, bid_prices, ask_prices, volumes, bid_points, ask_points, quote_req_id, quote_id, lp_name, status, tenor) VALUES ");
                            }
                        }
                    }
                }).readOne()) {
                // nothing to do
            }

                // Execute any remaining batch
                if (sqlBuilder.length() > 0) {
                    executeBatch(client, sqlBuilder);
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
            log.info("Table `market_data` is ready.");
        } catch (Exception e) {
            log.error("Failed to create table `market_data`", e);
        }
    }

    private static void executeBatch(Client client, StringBuilder sqlBuilder) {
        // Remove the last comma
        sqlBuilder.setLength(sqlBuilder.length() - 1);

        CompletableFuture.runAsync(() -> {
            try (QueryResponse response = client.query(sqlBuilder.toString()).get(3, TimeUnit.SECONDS)) {
                log.info("Batch inserted successfully.");
            } catch (Exception e) {
                log.error("Failed to execute batch", e);
            }
        });
    }
}