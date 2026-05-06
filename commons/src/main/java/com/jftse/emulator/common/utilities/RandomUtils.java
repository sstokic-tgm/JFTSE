package com.jftse.emulator.common.utilities;

import java.security.SecureRandom;

public class RandomUtils {

    public static final SecureRandom random;

    static {
        random = new SecureRandom();
        random.setSeed(System.currentTimeMillis());

    }

    public static String getUUID() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(Integer.toHexString(random.nextInt(16)));
        }
        return sb.toString();
    }
}
