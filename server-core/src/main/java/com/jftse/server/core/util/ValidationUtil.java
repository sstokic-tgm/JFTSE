package com.jftse.server.core.util;

import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("\"password\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern SENSITIVE_NUMERIC_PATTERN = Pattern.compile("\"(decKey|encKey|decTblIdx|encTblIdx)\"\\s*:\\s*(\\d+)");

    private ValidationUtil() {
    }

    public static String sanitizeLogForEncode(String input) {
        String sanitized = sanitizeLogForDecode(input);
        sanitized = SENSITIVE_NUMERIC_PATTERN.matcher(sanitized).replaceAll("\"$1\":****");
        return sanitized;
    }

    public static String sanitizeLogForDecode(String input) {
        String sanitized = PASSWORD_PATTERN.matcher(input).replaceAll("\"password\":\"****\"");
        sanitized = TOKEN_PATTERN.matcher(sanitized).replaceAll("\"token\":\"****\"");
        return sanitized;
    }
}
