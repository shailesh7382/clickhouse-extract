package experiment.clickhouse.service;

import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.insert.InsertResponse;
import com.clickhouse.client.api.insert.InsertSettings;
import com.clickhouse.client.api.metrics.ServerMetrics;
import com.clickhouse.data.ClickHouseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Example class showing how to pass raw data stream to the new ClickHouse client.
 * Input data is passed as InputStream to the {@link com.clickhouse.client.api.Client#insert(String, InputStream, ClickHouseFormat, InsertSettings)}
 * and the format is specified there too.
 */
public class Stream2DbWriter {

    private static final Logger log = LoggerFactory.getLogger(Stream2DbWriter.class);
    private static final String TABLE_NAME = "hacker_news_articles";

    private final Client client;
    private final String database;

    /**
     * Constructor to initialize the ClickHouse client.
     *
     * @param endpoint ClickHouse server endpoint
     * @param user     Username for ClickHouse
     * @param password Password for ClickHouse
     * @param database Database name
     */
    public Stream2DbWriter(String endpoint, String user, String password, String database) {
        this.client = new Client.Builder()
                .addEndpoint(endpoint)
                .setUsername(user)
                .setPassword(password)
                .compressServerResponse(true)
                .setDefaultDatabase(database)
                .build();
        this.database = database;
    }

    /**
     * Checks if the ClickHouse server is alive.
     *
     * @return true if the server is alive, false otherwise
     */
    public boolean isServerAlive() {
        log.info("Pinging ClickHouse server to check if it is alive");
        return client.ping();
    }

    /**
     * Resets the table in the ClickHouse database.
     */
    public void resetTable() {
        log.info("Resetting the table: {}", TABLE_NAME);
        try (InputStream initSql = Stream2DbWriter.class.getResourceAsStream("/simple_writer_init.sql")) {
            // Drop the table if it exists
            client.query("DROP TABLE IF EXISTS " + TABLE_NAME).get(3, TimeUnit.SECONDS);

            // Read and execute the SQL file to create the table
            BufferedReader reader = new BufferedReader(new InputStreamReader(initSql));
            String sql = reader.lines().collect(Collectors.joining("\n"));
            log.info("Executing Create Table SQL: {}", sql);
            client.query(sql).get(3, TimeUnit.SECONDS);
            log.info("Table initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize table", e);
        }
    }

    /**
     * Inserts data from an InputStream in JSONEachRow format into the ClickHouse table.
     *
     * @param inputStream InputStream of JSONEachRow formatted data
     */
    public void insertData_JSONEachRowFormat(InputStream inputStream) {
        log.info("Inserting data into table: {} using JSONEachRow format", TABLE_NAME);
        InsertSettings insertSettings = new InsertSettings();
        try (InsertResponse response = client.insert(TABLE_NAME, inputStream, ClickHouseFormat.JSONEachRow, insertSettings).get(3, TimeUnit.SECONDS)) {
            log.info("Insert finished: {} rows written", response.getMetrics().getMetric(ServerMetrics.NUM_ROWS_WRITTEN).getLong());
        } catch (Exception e) {
            log.error("Failed to write JSONEachRow data", e);
            throw new RuntimeException(e);
        }
    }
}