// ClickhouseController.java
package experiment.clickhouse.controller;

import experiment.clickhouse.service.ClickhouseAccess;
import experiment.clickhouse.service.fix.LpPriceEvent;
import experiment.clickhouse.service.fix.Pricer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClickhouseController {

    private final ClickhouseAccess clickhouseAccess;
    private final Pricer pricer;

    public ClickhouseController(ClickhouseAccess clickhouseAccess, Pricer pricer) {
        this.clickhouseAccess = clickhouseAccess;
        this.pricer = pricer;
    }

    @GetMapping("/status")
    public String getStatus() {
        return clickhouseAccess.checkStatus();
    }

    @GetMapping("/start-pricing")
    public String startPricing() {
        pricer.startPricing();
        return "Pricing started";
    }

    @GetMapping("/historical-prices")
    public List<LpPriceEvent> getHistoricalPrices(@RequestParam String ccyPair, @RequestParam String lpName, @RequestParam String startDate, @RequestParam String endDate) {
        return clickhouseAccess.getHistoricalPrices(ccyPair, lpName, startDate, endDate);
    }
}