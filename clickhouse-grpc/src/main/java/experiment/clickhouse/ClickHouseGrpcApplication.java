package experiment.clickhouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "experiment.clickhouse")
public class ClickHouseGrpcApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClickHouseGrpcApplication.class, args);
    }
}