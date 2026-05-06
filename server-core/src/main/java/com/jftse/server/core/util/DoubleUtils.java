package com.jftse.server.core.util;

public final class DoubleUtils {
    public static double shortToDouble(short value) {
        return value * 0.01;
    }

    public static short doubleToShort(double value) {
        return (short) (value * 100.0);
    }
}
