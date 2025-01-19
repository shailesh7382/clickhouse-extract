package com.example.marketdata;

public interface MarketDataUpdateListener {
    void onMarketDataUpdate(MarketData marketData);
    void onFXOrderUpdate(FXOrder fxOrder);
}