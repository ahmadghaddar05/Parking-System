package com.dslab.parking.model;

public class CreditCard {
    private int cardId;
    private int driverId;
    private String cardNickname;
    private String cardNumber;
    private String cardExpiry;
    private String cardCvv;
    private String cardType;
    private boolean isDefault;

    public CreditCard() {}

    public int getCardId() { return cardId; }
    public void setCardId(int cardId) { this.cardId = cardId; }

    public int getDriverId() { return driverId; }
    public void setDriverId(int driverId) { this.driverId = driverId; }

    public String getCardNickname() { return cardNickname; }
    public void setCardNickname(String cardNickname) { this.cardNickname = cardNickname; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCardExpiry() { return cardExpiry; }
    public void setCardExpiry(String cardExpiry) { this.cardExpiry = cardExpiry; }

    public String getCardCvv() { return cardCvv; }
    public void setCardCvv(String cardCvv) { this.cardCvv = cardCvv; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
}
