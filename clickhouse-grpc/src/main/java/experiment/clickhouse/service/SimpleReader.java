package experiment.clickhouse.service;

import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.data_formats.ClickHouseBinaryFormatReader;
import com.clickhouse.client.api.metrics.ClientMetrics;
import com.clickhouse.client.api.query.GenericRecord;
import com.clickhouse.client.api.query.QueryResponse;
import com.clickhouse.client.api.query.Records;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleReader {

    private static final Logger log = Logger.getLogger(SimpleReader.class.getName());

    private static final String TABLE_NAME = "news_articles";

    Client client;

    public SimpleReader(String endpoint, String user, String password, String database) {
        // Create sample_hacker_news_posts.json lightweight object to interact with ClickHouse server
        Client.Builder clientBuilder = new Client.Builder()
                .addEndpoint(endpoint)
                .setUsername(user)
                .setPassword(password)
                .compressServerResponse(true)
                .setDefaultDatabase(database);

        this.client = clientBuilder.build();
    }

    public boolean isServerAlive() {
        return client.ping();
    }

    public void readDataUsingBinaryFormat() {
        log.info("Reading data from table: " + TABLE_NAME);
        final String sql = "select * from " + TABLE_NAME + " where title <> '' limit 10";

        // Default format is RowBinaryWithNamesAndTypesFormatReader so reader have all information about columns
        try (QueryResponse response = client.query(sql).get(3, TimeUnit.SECONDS);) {

            // Create sample_hacker_news_posts.json reader to access the data in sample_hacker_news_posts.json convenient way
            ClickHouseBinaryFormatReader reader = client.newBinaryFormatReader(response);

            while (reader.hasNext()) {
                reader.next(); // Read the next record from stream and parse it

                // get values
                double id = reader.getDouble("id");
                String title = reader.getString("title");
                String url = reader.getString("url");

                log.info("id: " + id + ", title: " + title + ", url: " + url);
            }

            log.info("Data read successfully: " + response.getMetrics().getMetric(ClientMetrics.OP_DURATION).getLong() + " ms");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to read data", e);
        }
        // Response object must be closed to release resources
    }

    public void readDataAll() {
        try {
            log.info("Reading whole table and process record by record");
            final String sql = "select * from " + TABLE_NAME + " where title <> ''";

            // Read whole result set and process it record by record
            client.queryAll(sql).forEach(row -> {
                double id = row.getDouble("id");
                String title = row.getString("title");
                String url = row.getString("url");

                log.info("id: " + id + ", title: " + title + ", url: " + url);
            });
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to read data", e);
        }
    }

    public void readData() {
        log.info("Reading data from table: " + TABLE_NAME + " using Records iterator");
        final String sql = "select * from " + TABLE_NAME + " where title <> '' limit 10";
       try (Records records = client.queryRecords(sql).get(3, TimeUnit.SECONDS);) {

            // Get some metrics
            log.info("Data read successfully: " + TimeUnit.NANOSECONDS.toMillis(records.getServerTime()) + " ms");
            log.info("Total rows: " + records.getResultRows());

            // Iterate thru records
            for (GenericRecord record : records) {
                double id = record.getDouble("id");
                String title = record.getString("title");
                String url = record.getString("url");

                log.info("id: " + id + ", title: " + title + ", url: " + url);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to read data", e);
        }
    }
}