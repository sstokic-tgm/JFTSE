package com.jftse.server.core.constants;

public abstract class ShopItemFlags {
    public final static int MINT = 1 << 0;
    public final static int NEW = 1 << 1;
    public final static int HIT = 1 << 2;
    public final static int UNK0 = 1 << 3;
    public final static int UNK1_NOT_USED = 1 << 4;
    public final static int EVENT = 1 << 5;
    public final static int COUPLE = 1 << 6;
    public final static int NO_BUY = 1 << 7;

    public static byte getPriceTypeFlag(String priceType) {
        return priceType.equals("GOLD") ? (byte) 0 : (byte) MINT;
    }

    public static byte add(byte currentFlags, int flagToAdd) {
        return (byte) (currentFlags | flagToAdd);
    }
}
