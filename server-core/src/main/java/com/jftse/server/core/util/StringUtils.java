package com.jftse.server.core.util;

import com.google.gson.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern PACKET_LINE = Pattern.compile(
            "^\\s*(\\w+)\\s*\\{\\s*\"id\":\\s*\"?([^\",]+)\"?,\\s*\"len\":\\s*([^,]+),\\s*\"data\":\\s*(\\{.*})\\s*}$",
            Pattern.DOTALL);
    private static final Pattern PACKET_BLOCK = Pattern.compile("(\\w+)\\s*\\{", Pattern.DOTALL);

    public static String pretty(String gsonString) {
        if (gsonString == null || gsonString.isBlank()) {
            return gsonString;
        }

        try {
            JsonElement jsonElement = JsonParser.parseString(gsonString);
            return PRETTY_GSON.toJson(jsonElement);
        } catch (Exception ignored) {
        }

        List<String> packets = splitPackets(gsonString);
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String p : packets) {
            sb.append(prettySingle(p));
            if (count < packets.size() - 1) {
                sb.append(",\n");
            }
            count++;
        }
        return sb.toString().trim();
    }

    private static List<String> splitPackets(String gsonString) {
        Matcher m = PACKET_BLOCK.matcher(gsonString);
        List<Integer> indices = new java.util.ArrayList<>();
        while (m.find()) {
            indices.add(m.start());
        }
        indices.add(gsonString.length());

        List<String> packets = new java.util.ArrayList<>();
        for (int i = 0; i < indices.size() - 1; i++) {
            String chunk = gsonString.substring(indices.get(i), indices.get(i + 1)).trim();
            if (!chunk.isEmpty()) {
                if (chunk.endsWith(",")) chunk = chunk.substring(0, chunk.length() - 1).trim();
                packets.add(chunk);
            }
        }
        return packets;
    }

    private static String prettySingle(String gsonString) {
        Matcher m = PACKET_LINE.matcher(gsonString);
        if (m.find()) {
            String type = m.group(1).trim();
            String id = m.group(2).trim();
            String len = m.group(3).trim();
            String data = m.group(4).trim();

            JsonObject root = new JsonObject();
            root.addProperty("id", id);
            if (len.matches("-?\\d+")) {
                root.addProperty("len", Long.parseLong(len));
            } else {
                root.addProperty("len", len);
            }

            // Attempt to parse data as JSON
            try {
                JsonElement dataElement = JsonParser.parseString(data);
                root.add("data", dataElement);
            } catch (Exception e) {
                // fallback if malformed
                JsonObject dataObj = new JsonObject();
                dataObj.addProperty("raw", data);
                root.add("data", dataObj);
            }

            return type + " " + PRETTY_GSON.toJson(root);
        }
        return gsonString;
    }
}