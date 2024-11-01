package experiment.clickhouse.service;

import experiment.clickhouse.service.fix.LpPriceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
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

        log.info("ClickhouseAccess run method completed");
        Runtime.getRuntime().exit(0);
    }

    public String checkStatus() {
        return isServerAlive() ? "ClickHouse server is alive" : "ClickHouse server is not alive";
    }

    public List<LpPriceEvent> getHistoricalPrices(String ccyPair, String lpName, String startDate, String endDate) {
        return null /* writer.getHistoricalPrices(ccyPair, lpName, startDate, endDate) */;
    }

    private boolean isServerAlive() {
       return writer.isServerAlive();
    }

   

    public void insertLpPriceEvent(LpPriceEvent event) {
        writer.insertLpPriceEvent(event);
    }
}