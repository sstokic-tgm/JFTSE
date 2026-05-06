package com.jftse.emulator.server.core.utils;

public final class Fibonacci {
    private static final int MAX_LONG_FIB_INDEX = 92;
    private static final int MAX_INT_FIB_INDEX = 46;

    private static final long[] LONG_FIB_CACHE = new long[MAX_LONG_FIB_INDEX + 1];
    private static final int[] INT_FIB_CACHE = new int[MAX_INT_FIB_INDEX + 1];

    static {
        LONG_FIB_CACHE[0] = 0;
        LONG_FIB_CACHE[1] = 1;
        for (int i = 2; i <= MAX_LONG_FIB_INDEX; i++) {
            LONG_FIB_CACHE[i] = LONG_FIB_CACHE[i - 1] + LONG_FIB_CACHE[i - 2];
        }

        for (int i = 0; i <= MAX_INT_FIB_INDEX; i++) {
            INT_FIB_CACHE[i] = (int) LONG_FIB_CACHE[i];
        }
    }

    private Fibonacci() {
        // empty
    }

    public static long fibL(int n) {
        if (n < 0) return 0;
        if (n > MAX_LONG_FIB_INDEX) return Long.MAX_VALUE;
        return LONG_FIB_CACHE[n];
    }

    public static int fibI(int n) {
        if (n < 0) return 0;
        if (n > MAX_INT_FIB_INDEX) return Integer.MAX_VALUE;
        return INT_FIB_CACHE[n];
    }
}
