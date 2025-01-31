package com.example.marketdata;

import net.openhft.chronicle.wire.AbstractEventCfg;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MarketData extends AbstractEventCfg<MarketData> {
    private String ccyPair;
    private List<Double> bidPrices;
    private List<Double> askPrices;
    private List<Double> volumes;
    private List<Double> bidPoints;
    private List<Double> askPoints;
    private String quoteReqId;
    private String quoteId;
    private String lpName;
    private String status;
    private String tenor;

    public MarketData(LocalDateTime eventTime, String ccyPair, List<Double> bidPrices, List<Double> askPrices, List<Double> volumes, List<Double> bidPoints, List<Double> askPoints, String quoteReqId, String quoteId, String lpName, String status, String tenor) {
        this.ccyPair = ccyPair;
        this.bidPrices = bidPrices;
        this.askPrices = askPrices;
        this.volumes = volumes;
        this.bidPoints = bidPoints;
        this.askPoints = askPoints;
        this.quoteReqId = quoteReqId;
        this.quoteId = quoteId;
        this.lpName = lpName;
        this.status = status;
        this.tenor = tenor;
//        this.eventTime(eventTime.toEpochSecond(ZoneOffset.UTC) * 1_000_000_000L + eventTime.getNano());
        this.eventTimeNow();
        this.serviceId(UUID.randomUUID().toString());
        this.eventId("marketData");
    }

    public MarketData(LocalDateTime eventTime, String ccyPair, double[] bidPrices, double[] askPrices, double[] volumes, double[] bidPoints, double[] askPoints, String quoteReqId, String quoteId, String lpName, String status, String tenor) {
        List<Double> bidPricesList = new ArrayList<>();
        for (double bidPrice : bidPrices) {
            bidPricesList.add(bidPrice);
        }
        List<Double> askPricesList = new ArrayList<>();
        for (double askPrice : askPrices) {
            askPricesList.add(askPrice);
        }
        List<Double> volumesList = new ArrayList<>();
        for (double volume : volumes) {
            volumesList.add(volume);
        }
        List<Double> bidPointsList = new ArrayList<>();
        for (double bidPoint : bidPoints) {
            bidPointsList.add(bidPoint);
        }
        List<Double> askPointsList = new ArrayList<>();
        for (double askPoint : askPoints) {
            askPointsList.add(askPoint);
        }
        this.ccyPair = ccyPair;
        this.bidPrices = bidPricesList;
        this.askPrices = askPricesList;
        this.volumes = volumesList;
        this.bidPoints = bidPointsList;
        this.askPoints = askPointsList;
        this.quoteReqId = quoteReqId;
        this.quoteId = quoteId;
        this.lpName = lpName;
        this.status = status;
        this.tenor = tenor;
        this.eventTime(eventTime.toEpochSecond(ZoneOffset.UTC) * 1_000_000_000L + eventTime.getNano());
    }

    // Getters
    public String getCcyPair() {
        return ccyPair;
    }

    public List<Double> getBidPrices() {
        return bidPrices;
    }

    public List<Double> getAskPrices() {
        return askPrices;
    }

    public List<Double> getVolumes() {
        return volumes;
    }

    public List<Double> getBidPoints() {
        return bidPoints;
    }

    public List<Double> getAskPoints() {
        return askPoints;
    }

    public String getQuoteReqId() {
        return quoteReqId;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public String getLpName() {
        return lpName;
    }

    public String getStatus() {
        return status;
    }

    public String getTenor() {
        return tenor;
    }

    // Setters
    public void setCcyPair(String ccyPair) {
        this.ccyPair = ccyPair;
    }

    public void setBidPrices(List<Double> bidPrices) {
        this.bidPrices = bidPrices;
    }

    public void setAskPrices(List<Double> askPrices) {
        this.askPrices = askPrices;
    }

    public void setVolumes(List<Double> volumes) {
        this.volumes = volumes;
    }

    public void setBidPoints(List<Double> bidPoints) {
        this.bidPoints = bidPoints;
    }

    public void setAskPoints(List<Double> askPoints) {
        this.askPoints = askPoints;
    }

    public void setQuoteReqId(String quoteReqId) {
        this.quoteReqId = quoteReqId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    public void setLpName(String lpName) {
        this.lpName = lpName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTenor(String tenor) {
        this.tenor = tenor;
    }

    @Override
    public String toString() {
        return "MarketData{" +
                "ccyPair='" + ccyPair + '\'' +
                ", bidPrices=" + bidPrices +
                ", askPrices=" + askPrices +
                ", volumes=" + volumes +
                ", bidPoints=" + bidPoints +
                ", askPoints=" + askPoints +
                ", quoteReqId='" + quoteReqId + '\'' +
                ", quoteId='" + quoteId + '\'' +
                ", lpName='" + lpName + '\'' +
                ", status='" + status + '\'' +
                ", tenor='" + tenor + '\'' +
                '}';
    }
}