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
        String[] lpNames = {"REU", "JPM", "SCB", "UBS"};
        double[][] quantities = {
            {1000000, 2000000, 3000000},
            {1500000, 2500000, 3500000},
            {1200000, 2200000, 3200000},
            {1300000, 2300000, 3300000}
        };
        try {
            while (!oneMonthAgo.isAfter(now)) {
                if (isWeekday(oneMonthAgo) && !holidays.contains(oneMonthAgo.toLocalDate())) {
                    for (int i = 0; i < lpNames.length; i++) {
                        counter++;
                        LpPriceEvent event = generateLpPriceEvent(oneMonthAgo, lpNames[i], quantities[i]);
                        clickhouseAccess.insertLpPriceEvent(event);
                        log.info("Sending event: {} - {}", counter, event);
                    }
                }
                oneMonthAgo = oneMonthAgo.plusMinutes(1); // Adjust the interval as needed
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LpPriceEvent generateLpPriceEvent(LocalDateTime timestamp, String lpName, double[] quantities) {
        String uuid = UUID.randomUUID().toString();
        String ccyPair = currencyPairs[random.nextInt(currencyPairs.length)];
        double midPrice = generateMidPrice(ccyPair);

        // Generate bid and ask prices based on the length of quantities array
        double[] bidPrices = new double[quantities.length];
        double[] askPrices = new double[quantities.length];
        for (int i = 0; i < quantities.length; i++) {
            bidPrices[i] = roundPrice(midPrice - (0.0001 * (i + 1)), ccyPair);
            askPrices[i] = roundPrice(midPrice + (0.0001 * (i + 1)), ccyPair);
        }

        String tenor = "SPOT";
        LocalDate localDate = timestamp.toLocalDate();

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