package experiment.clickhouse.service;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ClickhouseAccess {

    private static final Logger log = Logger.getLogger(ClickhouseAccess.class.getName());

    private final String endpoint;
    private final String user;
    private final String password;
    private final String database;

    public ClickhouseAccess() {
        // Initialize connection parameters from system properties
        this.endpoint = System.getProperty("chEndpoint", "http://localhost:8123");
        this.user = System.getProperty("chUser", "default");
        this.password = System.getProperty("chPassword", "password123");
        this.database = System.getProperty("chDatabase", "default");
    }

    public static void main(String[] args) {
        ClickhouseAccess access = new ClickhouseAccess();
        access.run();
    }

    public void run() {
        log.info("Starting ClickhouseAccess run method");

        // Check if the server is alive
        if (!isServerAlive()) {
            log.severe("ClickHouse server is not alive");
            Runtime.getRuntime().exit(-503);
        }

        // Reset the table
        resetTable();

        // Insert data from JSON file
        insertDataFromJson();

        // Read data in various formats
        readData();
        readDataAsText();

        // Insert data using POJO
        insertDataUsingPOJO();

        log.info("ClickhouseAccess run method completed");
        Runtime.getRuntime().exit(0);
    }

    private boolean isServerAlive() {
        log.info("Checking if ClickHouse server is alive");
        Stream2DbWriter writer = new Stream2DbWriter(endpoint, user, password, database);
        boolean isAlive = writer.isServerAlive();
        if (isAlive) {
            log.info("ClickHouse server is alive");
        } else {
            log.warning("ClickHouse server is not alive");
        }
        return isAlive;
    }

    private void resetTable() {
        log.info("Resetting the table in ClickHouse");
        Stream2DbWriter writer = new Stream2DbWriter(endpoint, user, password, database);
        writer.resetTable();
    }

    private void insertDataFromJson() {
        log.info("Inserting data from resources/sample_hacker_news_posts.json");
        try (InputStream is = ClickhouseAccess.class.getResourceAsStream("/sample_hacker_news_posts.json")) {
            Stream2DbWriter writer = new Stream2DbWriter(endpoint, user, password, database);
            writer.insertData_JSONEachRowFormat(is);
            log.info("Data inserted successfully from JSON file");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to insert data from JSON file", e);
        }
    }

    private void readData() {
        log.info("Reading data using SimpleReader");
        SimpleReader reader = new SimpleReader(endpoint, user, password, database);
        reader.readDataUsingBinaryFormat();
        reader.readDataAll();
        reader.readData();
    }

    private void readDataAsText() {
        log.info("Reading data in text formats using TextFormatsReader");
        TextFormatsReader textFormatsReader = new TextFormatsReader(endpoint, user, password, database);
        textFormatsReader.readAsJsonEachRow();
        textFormatsReader.readAsJsonEachRowButGSon();
        textFormatsReader.readJSONEachRowIntoArrayOfObject();
        textFormatsReader.readJSONEachRowIntoArrayOfObjectGson();
        textFormatsReader.readAsCSV();
        textFormatsReader.readAsTSV();
    }

    private void insertDataUsingPOJO() {
        log.info("Inserting data using POJO");
        ObjectDbWriter pojoWriter = new ObjectDbWriter(endpoint, user, password, database);
        pojoWriter.resetTable();
        for (int i = 0; i < 10; i++) {
            pojoWriter.submit(new ArticleViewEvent(11132929d, LocalDateTime.now(), UUID.randomUUID().toString()));
        }
        pojoWriter.printLastEvents();
        log.info("Data inserted successfully using POJO");
    }
}