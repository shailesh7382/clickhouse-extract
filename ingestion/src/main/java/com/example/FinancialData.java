package com.example;

import net.openhft.chronicle.wire.SelfDescribingMarshallable;

public class FinancialData extends SelfDescribingMarshallable {
    private String date;
    private String transactionId;
    private double amount;
    private String currency;

    public FinancialData(String date, String transactionId, double amount, String currency) {
        this.date = date;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
    }

    // Getters and setters (if needed)
}