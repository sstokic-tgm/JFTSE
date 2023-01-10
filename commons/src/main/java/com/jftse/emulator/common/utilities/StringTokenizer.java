package com.jftse.emulator.common.utilities;

import java.util.Arrays;
import java.util.List;

public class StringTokenizer {
    private final String DELIMITER;
    private final String data;

    public StringTokenizer(String data, String delimiter) {
        this.data = data;
        this.DELIMITER = delimiter;
    }

    public List<String> get() {
        return Arrays.asList(data.split(DELIMITER));
    }

    public static String tokenizeOf(String data, int offset, String delimiter) {
        return new StringTokenizer(data, delimiter).get().get(offset);
    }
}
