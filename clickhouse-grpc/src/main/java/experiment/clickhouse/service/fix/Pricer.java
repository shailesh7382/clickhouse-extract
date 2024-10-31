package experiment.clickhouse.service.fix;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Pricer {

    private final Random random = new Random();
    private final String[] currencyPairs = {"EURUSD", "AUDUSD", "USDJPY", "USDSGD", "GBPUSD"};

    public LpPriceEvent generateLpPriceEvent() {
        LocalDateTime timestamp = LocalDateTime.now();
        String uuid = UUID.randomUUID().toString();
        String ccyPair = currencyPairs[random.nextInt(currencyPairs.length)];
        double midPrice = generateMidPrice(ccyPair);
        double[] bidPrices = {
            roundToFiveDecimalPlaces(midPrice - 0.0001),
            roundToFiveDecimalPlaces(midPrice - 0.0002),
            roundToFiveDecimalPlaces(midPrice - 0.0003)
        };
        double[] askPrices = {
            roundToFiveDecimalPlaces(midPrice + 0.0001),
            roundToFiveDecimalPlaces(midPrice + 0.0002),
            roundToFiveDecimalPlaces(midPrice + 0.0003)
        };
        double[] quantities = {1000000, 2000000, 3000000};
        String tenor = "SPOT";
        LocalDate localDate = LocalDate.now();
        String lpName = "REU";

        LpPriceEvent event = new LpPriceEvent(timestamp, uuid);
        event.setBidPrices(bidPrices);
        event.setAskPrices(askPrices);
        event.setQuantities(quantities);
        event.setCcyPair(ccyPair);
        event.setTenor(tenor);
        event.setLocalDate(localDate);
        event.setLpName(lpName);

        return event;
    }

    private double generateMidPrice(String ccyPair) {
        switch (ccyPair) {
            case "AUDUSD":
                return 0.7500 + (random.nextDouble() - 0.5) * 0.01;
            case "USDJPY":
                return 110.00 + (random.nextDouble() - 0.5) * 1.00;
            case "USDSGD":
                return 1.3500 + (random.nextDouble() - 0.5) * 0.01;
            case "GBPUSD":
                return 1.3000 + (random.nextDouble() - 0.5) * 0.01;
            case "EURUSD":
            default:
                return 1.1000 + (random.nextDouble() - 0.5) * 0.01;
        }
    }

    private double roundToFiveDecimalPlaces(double value) {
        return Math.round(value * 100000.0) / 100000.0;
    }

    public static void main(String[] args) {
        Pricer pricer = new Pricer();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            LpPriceEvent event = pricer.generateLpPriceEvent();
            System.out.println(event);
        };

        executor.scheduleAtFixedRate(task, 0, 250, TimeUnit.MILLISECONDS);
    }
}