package com.jftse.emulator.server.database.model.messenger;

public enum EParcelType {
    None((byte) 0), Gold((byte) 1), CashOnDelivery((byte) 2);

    private final Byte value;

    EParcelType(Byte value) {
        this.value = value;
    }

    public Byte getValue() {
        return value;
    }
}