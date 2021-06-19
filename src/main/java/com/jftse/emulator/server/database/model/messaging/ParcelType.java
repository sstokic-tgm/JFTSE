package com.jftse.emulator.server.database.model.messaging;

public enum ParcelType {
    None(0), Gold(10), CashOnDelivery(5);

    private final int value;

    ParcelType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}