package experiment.clickhouse.service.fix;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

public class LpPriceEvent {
    private LocalDateTime timestamp;
    private String uuid;
    private double[] bidPrices;
    private double[] askPrices;
    private double[] quantities;
    private String ccyPair;
    private String tenor;
    private LocalDate localDate;
    private String lpName;

    public LpPriceEvent(LocalDateTime timestamp, String uuid) {
        this.timestamp = timestamp;
        this.uuid = uuid;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public double[] getBidPrices() {
        return bidPrices;
    }

    public void setBidPrices(double[] bidPrices) {
        this.bidPrices = bidPrices;
    }

    public double[] getAskPrices() {
        return askPrices;
    }

    public void setAskPrices(double[] askPrices) {
        this.askPrices = askPrices;
    }

    public double[] getQuantities() {
        return quantities;
    }

    public void setQuantities(double[] quantities) {
        this.quantities = quantities;
    }

    public String getCcyPair() {
        return ccyPair;
    }

    public void setCcyPair(String ccyPair) {
        this.ccyPair = ccyPair;
    }

    public String getTenor() {
        return tenor;
    }

    public void setTenor(String tenor) {
        this.tenor = tenor;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public String getLpName() {
        return lpName;
    }

    public void setLpName(String lpName) {
        this.lpName = lpName;
    }

    @Override
    public String toString() {
        return "LpPriceEvent{" +
                "timestamp=" + timestamp +
                ", uuid='" + uuid + '\'' +
                ", bidPrices=" + Arrays.toString(bidPrices) +
                ", askPrices=" + Arrays.toString(askPrices) +
                ", quantities=" + Arrays.toString(quantities) +
                ", ccyPair='" + ccyPair + '\'' +
                ", tenor='" + tenor + '\'' +
                ", localDate=" + localDate +
                ", lpName='" + lpName + '\'' +
                '}';
    }
}