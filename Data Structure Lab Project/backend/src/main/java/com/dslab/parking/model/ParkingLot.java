package com.dslab.parking.model;

import java.math.BigDecimal;

public class ParkingLot {
    private int lotId;
    private String lotName;
    private String location;
    private String openingHours;
    private BigDecimal entryFee;
    private BigDecimal hourlyRate;
    private int spotCount;
    private BigDecimal lat;
    private BigDecimal lng;
    private String currency;

    public ParkingLot() {}

    public int getLotId() { return lotId; }
    public void setLotId(int lotId) { this.lotId = lotId; }

    public String getLotName() { return lotName; }
    public void setLotName(String lotName) { this.lotName = lotName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }

    public BigDecimal getEntryFee() { return entryFee; }
    public void setEntryFee(BigDecimal entryFee) { this.entryFee = entryFee; }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

    public int getSpotCount() { return spotCount; }
    public void setSpotCount(int spotCount) { this.spotCount = spotCount; }

    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }

    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
