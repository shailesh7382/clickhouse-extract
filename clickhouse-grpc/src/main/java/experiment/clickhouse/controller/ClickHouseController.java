package experiment.clickhouse.controller;

import experiment.clickhouse.service.ClickHouseGrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClickHouseController {

    @Autowired
    private ClickHouseGrpcService clickHouseGrpcService;

    @GetMapping("/status")
    public String getStatus() {
        return clickHouseGrpcService.getStatus();
    }
}