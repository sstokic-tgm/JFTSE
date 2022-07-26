package com.jftse.emulator.server.database.model.auctionhouse;

public enum PriceType {
    GOLD,
    MINT,
    COUPLE_POINTS;

    public String getName() {
        return toString();
    }
}
