package experiment.clickhouse.service.fix;

import experiment.clickhouse.service.ClickhouseAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Service
public class Pricer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(Pricer.class);
    private final Random random = new Random();
    private final String[] currencyPairs = {"EURUSD", "AUDUSD", "USDJPY", "USDSGD", "GBPUSD"};
    private final Set<LocalDate> holidays = new HashSet<>();

    @Autowired
    private ClickhouseAccess clickhouseAccess;

    public Pricer() {
        // Add holidays to the set
        // Singapore holidays
        holidays.add(LocalDate.of(2023, 1, 1));   // New Year's Day
        holidays.add(LocalDate.of(2023, 2, 1));   // Chinese New Year
        holidays.add(LocalDate.of(2023, 4, 7));   // Good Friday
        holidays.add(LocalDate.of(2023, 5, 1));   // Labour Day
        holidays.add(LocalDate.of(2023, 8, 9));   // National Day
        holidays.add(LocalDate.of(2023, 12, 25)); // Christmas

        // India holidays
        holidays.add(LocalDate.of(2023, 1, 26));  // Republic Day
        holidays.add(LocalDate.of(2023, 8, 15));  // Independence Day
        holidays.add(LocalDate.of(2023, 10, 2));  // Gandhi Jayanti
        holidays.add(LocalDate.of(2023, 11, 4));  // Diwali
        holidays.add(LocalDate.of(2023, 12, 25)); // Christmas

        // USA holidays
        holidays.add(LocalDate.of(2023, 1, 1));   // New Year's Day
        holidays.add(LocalDate.of(2023, 7, 4));   // Independence Day
        holidays.add(LocalDate.of(2023, 11, 23)); // Thanksgiving Day
        holidays.add(LocalDate.of(2023, 12, 25)); // Christmas
        holidays.add(LocalDate.of(2023, 1, 16));  // Martin Luther King Jr. Day
        holidays.add(LocalDate.of(2023, 2, 20));  // Presidents' Day
        holidays.add(LocalDate.of(2023, 5, 29));  // Memorial Day
        holidays.add(LocalDate.of(2023, 9, 4));   // Labor Day
    }

    @Override
    public void run(String... args) {
        startPricing();
    }

    public void startPricing() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        int counter = 0;
        try {
            while (!oneMonthAgo.isAfter(now)) {
                counter++;
                if (isWeekday(oneMonthAgo) && !holidays.contains(oneMonthAgo.toLocalDate())) {
                    LpPriceEvent event = generateLpPriceEvent(oneMonthAgo);
                    clickhouseAccess.insertLpPriceEvent(event);
                    log.info("Sending event: {} - {}", counter, event);
                }
                oneMonthAgo = oneMonthAgo.plusMinutes(1); // Adjust the interval as needed
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LpPriceEvent generateLpPriceEvent(LocalDateTime timestamp) {
        String uuid = UUID.randomUUID().toString();
        String ccyPair = currencyPairs[random.nextInt(currencyPairs.length)];
        double midPrice = generateMidPrice(ccyPair);
        double[] bidPrices = {
            roundPrice(midPrice - 0.0001, ccyPair),
            roundPrice(midPrice - 0.0002, ccyPair),
            roundPrice(midPrice - 0.0003, ccyPair)
        };
        double[] askPrices = {
            roundPrice(midPrice + 0.0001, ccyPair),
            roundPrice(midPrice + 0.0002, ccyPair),
            roundPrice(midPrice + 0.0003, ccyPair)
        };
        double[] quantities = {1000000, 2000000, 3000000};
        String tenor = "SPOT";
        LocalDate localDate = timestamp.toLocalDate();
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

    private double roundPrice(double value, String ccyPair) {
        if ("USDJPY".equals(ccyPair)) {
            return roundToThreeDecimalPlaces(value);
        } else {
            return roundToFiveDecimalPlaces(value);
        }
    }

    private double roundToFiveDecimalPlaces(double value) {
        return Math.round(value * 100000.0) / 100000.0;
    }

    private double roundToThreeDecimalPlaces(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private static boolean isWeekday(LocalDateTime dateTime) {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
}