package com.jftse.emulator.server.database.model.messaging;

public enum ParcelType {
    None((byte) 0), Gold((byte) 1), CashOnDelivery((byte) 2);

    private final Byte value;

    ParcelType(Byte value) {
        this.value = value;
    }

    public Byte getValue() {
        return value;
    }
}