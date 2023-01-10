package com.jftse.entities.database.model.auctionhouse;

public enum TradeStatus {
    CANCELED,
    IN_QUEUE,
    EXPIRED,
    BOUGHT,
    SOLD;

    public String getName() {
        return toString();
    }
}
