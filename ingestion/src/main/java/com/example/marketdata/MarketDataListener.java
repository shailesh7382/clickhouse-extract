package com.example.marketdata;

public interface MarketDataListener {
    void marketData(MarketData marketData);
    void fxOrder(FXOrder fxOrder);
}
