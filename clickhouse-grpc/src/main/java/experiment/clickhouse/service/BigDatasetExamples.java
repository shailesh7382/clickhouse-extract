package experiment.clickhouse.service;

import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.data_formats.ClickHouseBinaryFormatReader;
import com.clickhouse.client.api.metrics.ClientMetrics;
import com.clickhouse.client.api.query.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Example class demonstrating how to read a large dataset from ClickHouse.
 */
public class BigDatasetExamples {

    private static final Logger log = LoggerFactory.getLogger(BigDatasetExamples.class);

    private final String endpoint;
    private final String user;
    private final String password;
    private final String database;

    /**
     * Constructor to initialize the ClickHouse client parameters.
     *
     * @param endpoint ClickHouse server endpoint
     * @param user     Username for ClickHouse
     * @param password Password for ClickHouse
     * @param database Database name
     */
    public BigDatasetExamples(String endpoint, String user, String password, String database) {
        this.endpoint = endpoint;
        this.user = user;
        this.password = password;
        this.database = database;
    }

    /**
     * Reads a large set of numbers from the ClickHouse database.
     *
     * @param limit       Number of records to read
     * @param iterations  Number of iterations to perform
     * @param concurrency Number of concurrent threads
     */
    void readBigSetOfNumbers(int limit, int iterations, int concurrency) {
        Client client = new Client.Builder()
                .addEndpoint(endpoint)
                .setUsername(user)
                .setPassword(password)
                .setDefaultDatabase(database)
                .compressServerResponse(false)
                .compressClientRequest(false)
                .setLZ4UncompressedBufferSize(1048576)
                .useNewImplementation(true)
                .setSocketRcvbuf(1_000_000)
                .setClientNetworkBufferSize(1_000_000)
                .setMaxConnections(20)
                .build();

        try {
            log.info("Pinging ClickHouse server to warm up connections pool");
            client.ping(10); // warmup connections pool. required once per client.

            Runnable task = () -> {
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < iterations; i++) {
                    try {
                        long[] stats = doReadNumbersSet(client, limit);
                        for (long stat : stats) {
                            sb.append(stat).append(", ");
                        }
                        sb.append("\n");
                    } catch (Exception e) {
                        log.error("Failed to read dataset", e);
                    }
                }

                log.info("Read results: \n{}", sb.toString());
            };

            log.info("Starting dataset read with concurrency: {}", concurrency);
            if (concurrency == 1) {
                task.run();
            } else {
                ExecutorService executor = new ThreadPoolExecutor(concurrency, Integer.MAX_VALUE,
                        60L, TimeUnit.SECONDS,
                        new SynchronousQueue<>());

                for (int i = 0; i < concurrency; i++) {
                    executor.submit(task);
                }

                executor.shutdown();
                executor.awaitTermination(3, TimeUnit.MINUTES);
            }
        } catch (InterruptedException e) {
            log.error("Execution interrupted", e);
            throw new RuntimeException(e);
        } finally {
            client.close();
        }
    }

    /**
     * Executes the query to read a set of numbers and returns time statistics.
     *
     * @param client ClickHouse client
     * @param limit  Number of records to read
     * @return Array of time statistics [number of records, read time in ms, request initiation time in ms, server time in ms]
     */
    private long[] doReadNumbersSet(Client client, int limit) {
        final String query = DATASET_QUERY + " LIMIT " + limit;
        try (QueryResponse response = client.query(query).get(3000, TimeUnit.MILLISECONDS)) {
            ArrayList<NumbersRecord> result = new ArrayList<>();

            ClickHouseBinaryFormatReader reader = client.newBinaryFormatReader(response);

            long start = System.nanoTime();
            while (reader.next() != null) {
                result.add(new NumbersRecord(
                        reader.getUUID("id"),
                        reader.getLong("p1"),
                        reader.getBigInteger("number"),
                        reader.getFloat("p2"),
                        reader.getDouble("p3")
                ));
            }
            long duration = System.nanoTime() - start;

            return new long[]{result.size(), TimeUnit.NANOSECONDS.toMillis(duration), response.getMetrics().getMetric(ClientMetrics.OP_DURATION).getLong(),
                    TimeUnit.NANOSECONDS.toMillis(response.getServerTime())};
        } catch (Exception e) {
            log.error("Failed to fetch dataset", e);
            throw new RuntimeException("Failed to fetch dataset", e);
        }
    }

    private static final String DATASET_QUERY =
            "SELECT generateUUIDv4() as id, " +
                    "toUInt32(number) as p1, " +
                    "number,  " +
                    "toFloat32(number/100000) as p2, " +
                    "toFloat64(number/100000) as p3" +
                    " FROM system.numbers";

    public static void main(String[] args) {
        final String endpoint = System.getProperty("chEndpoint", "http://localhost:8123");
        final String user = System.getProperty("chUser", "default");
        final String password = System.getProperty("chPassword", "password123");
        final String database = System.getProperty("chDatabase", "default");

        BigDatasetExamples examples = new BigDatasetExamples(endpoint, user, password, database);

        log.info("Starting to read big set of numbers");
        examples.readBigSetOfNumbers(100_000, 100, 10);
        log.info("Completed reading big set of numbers");
    }
}