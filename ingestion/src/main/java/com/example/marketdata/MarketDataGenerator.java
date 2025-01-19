package com.example.marketdata;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MarketDataGenerator {
    private final List<String> ccyPairs = Arrays.asList("EURUSD", "GBPUSD", "USDJPY", "AUDUSD", "USDCAD", "NZDUSD", "USDCHF", "EURGBP");
    private final LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
    private final Random random = new Random();
    private final MarketDataUpdateListener listener;

    public MarketDataGenerator(MarketDataUpdateListener listener) {
        this.listener = listener;
    }

    public void generateMarketData(int count) {
        for (int i = 0; i < count; i++) {
            String ccyPair = ccyPairs.get(random.nextInt(ccyPairs.size()));
            LocalDateTime currentDate = startDate.plusSeconds(random.nextInt(60));

            double bidPrice = getLatestBidPrice(ccyPair);
            double askPrice = getLatestAskPrice(ccyPair);
            int decimalPlaces = getDecimalPlaces(ccyPair);

            List<Double> amountBands = generateRandomAmountBands();
            List<Double> bidPrices = generatePrices(bidPrice, amountBands.size(), true, decimalPlaces);
            List<Double> askPrices = generatePrices(askPrice, amountBands.size(), false, decimalPlaces);
            List<Double> bidPoints = generatePoints(amountBands.size());
            List<Double> askPoints = generatePoints(amountBands.size());

            MarketData md = new MarketData(
                    currentDate,
                    ccyPair,
                    bidPrices,
                    askPrices,
                    amountBands,
                    bidPoints,
                    askPoints,
                    "quoteReq_" + i,
                    "quote_" + i,
                    "LP1",
                    "status_" ,
                    "SP"
            );

            listener.onMarketDataUpdate(md);
        }
    }

    public void generateFXOrders(int count) {
        for (int i = 0; i < count; i++) {
            String ccyPair = ccyPairs.get(random.nextInt(ccyPairs.size()));
            LocalDateTime eventTime = startDate.plusSeconds(random.nextInt(60));
            double orderPrice = getLatestBidPrice(ccyPair);
            double orderAmount = 1_000_000L * (1 + random.nextInt(10));
            String orderId = "order_" + i;
            String orderType = random.nextBoolean() ? "BUY" : "SELL";
            String quoteId = "quote_" + i;
            String tenor = "SP";

            FXOrder order = new FXOrder(eventTime, ccyPair, orderPrice, orderAmount, orderId, orderType, quoteId, tenor);
            listener.onFXOrderUpdate(order);
        }
    }

    private double getLatestBidPrice(String ccyPair) {
        switch (ccyPair) {
            case "EURUSD": return 1.1000 + random.nextDouble() * 0.01;
            case "GBPUSD": return 1.3000 + random.nextDouble() * 0.01;
            case "USDJPY": return 110.00 + random.nextDouble();
            case "AUDUSD": return 0.7000 + random.nextDouble() * 0.01;
            case "USDCAD": return 1.2500 + random.nextDouble() * 0.01;
            case "NZDUSD": return 0.6500 + random.nextDouble() * 0.01;
            case "USDCHF": return 0.9000 + random.nextDouble() * 0.01;
            case "EURGBP": return 0.8500 + random.nextDouble() * 0.01;
            default: return 1.0000;
        }
    }

    private double getLatestAskPrice(String ccyPair) {
        switch (ccyPair) {
            case "EURUSD": return 1.1005 + random.nextDouble() * 0.01;
            case "GBPUSD": return 1.3005 + random.nextDouble() * 0.01;
            case "USDJPY": return 110.05 + random.nextDouble();
            case "AUDUSD": return 0.7005 + random.nextDouble() * 0.01;
            case "USDCAD": return 1.2505 + random.nextDouble() * 0.01;
            case "NZDUSD": return 0.6505 + random.nextDouble() * 0.01;
            case "USDCHF": return 0.9005 + random.nextDouble() * 0.01;
            case "EURGBP": return 0.8505 + random.nextDouble() * 0.01;
            default: return 1.0005;
        }
    }

    private int getDecimalPlaces(String ccyPair) {
        switch (ccyPair) {
            case "USDJPY": return 3;
            case "USDCHF": return 4;
            default: return 5;
        }
    }

    private List<Double> generateRandomAmountBands() {
        int numBands = 5 + random.nextInt(5);
        List<Double> amountBands = new ArrayList<>();
        for (int i = 0; i < numBands; i++) {
            amountBands.add((double) (1_000_000L * (i + 1)));
        }
        return amountBands;
    }

    private List<Double> generatePrices(double basePrice, int size, boolean isBid, int decimalPlaces) {
        List<Double> prices = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            prices.add(Math.round((basePrice + random.nextDouble() * 0.01) * Math.pow(10, decimalPlaces)) / Math.pow(10, decimalPlaces));
        }
        if (isBid) {
            prices.sort((a, b) -> Double.compare(b, a));
        } else {
            prices.sort(Double::compare);
        }
        return prices;
    }

    private List<Double> generatePoints(int size) {
        List<Double> points = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            points.add(Math.round(random.nextDouble() * 0.01 * 100000.0) / 100000.0);
        }
        return points;
    }
}