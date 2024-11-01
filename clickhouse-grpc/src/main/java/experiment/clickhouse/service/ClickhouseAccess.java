package experiment.clickhouse.service;

import experiment.clickhouse.service.fix.LpPriceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class ClickhouseAccess {

    private static final Logger log = Logger.getLogger(ClickhouseAccess.class.getName());

    private final String endpoint;
    private final String user;
    private final String password;
    private final String database;

    @Autowired
    private Stream2DbWriter writer;

    @Autowired
    public ClickhouseAccess(@Value("${clickhouse.endpoint}") String endpoint,
                            @Value("${clickhouse.user}") String user,
                            @Value("${clickhouse.password}") String password,
                            @Value("${clickhouse.database}") String database) {
        this.endpoint = endpoint;
        this.user = user;
        this.password = password;
        this.database = database;
    }

    public void run() {
        log.info("Starting ClickhouseAccess run method");

        // Check if the server is alive
        if (!isServerAlive()) {
            log.severe("ClickHouse server is not alive");
            Runtime.getRuntime().exit(-503);
        }

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
        boolean isAlive = writer.isServerAlive();
        if (isAlive) {
            log.info("ClickHouse server is alive");
        } else {
            log.warning("ClickHouse server is not alive");
        }
        return isAlive;
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

    public void insertLpPriceEvent(LpPriceEvent event) {
        writer.insertLpPriceEvent(event);
    }

    public String checkStatus() {
        return isServerAlive() ? "ClickHouse server is alive" : "ClickHouse server is not alive";
    }
}