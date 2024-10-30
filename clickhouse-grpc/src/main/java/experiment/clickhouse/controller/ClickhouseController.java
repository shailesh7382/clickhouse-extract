package experiment.clickhouse.controller;

import experiment.clickhouse.service.ClickhouseAccess;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClickhouseController {

    private final ClickhouseAccess clickhouseAccess;

    public ClickhouseController(ClickhouseAccess clickhouseAccess) {
        this.clickhouseAccess = clickhouseAccess;
    }

    @GetMapping("/status")
    public String getStatus() {
        return "1"/*clickhouseAccess.checkStatus()*/;
    }
}