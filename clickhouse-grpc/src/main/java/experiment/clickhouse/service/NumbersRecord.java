package experiment.clickhouse.service;

import java.math.BigInteger;
import java.util.UUID;

public class NumbersRecord {

    private UUID id;
    private long p1;
    private BigInteger number;
    private float p2;
    private double p3;

    public NumbersRecord() {
    }

    public NumbersRecord(UUID id, long p1, BigInteger number, float p2, double p3) {
        this.id = id;
        this.p1 = p1;
        this.number = number;
        this.p2 = p2;
        this.p3 = p3;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public long getP1() {
        return p1;
    }

    public void setP1(long p1) {
        this.p1 = p1;
    }

    public BigInteger getNumber() {
        return number;
    }

    public void setNumber(BigInteger number) {
        this.number = number;
    }

    public float getP2() {
        return p2;
    }

    public void setP2(float p2) {
        this.p2 = p2;
    }

    public double getP3() {
        return p3;
    }

    public void setP3(double p3) {
        this.p3 = p3;
    }

    @Override
    public String toString() {
        return "NumbersRecord{" +
                "id=" + id +
                ", p1=" + p1 +
                ", number=" + number +
                ", p2=" + p2 +
                ", p3=" + p3 +
                '}';
    }
}