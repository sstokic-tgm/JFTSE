package com.jftse.server.core.protocol;

import java.util.Arrays;

public final class PacketBuffer {
    public static final int MAX_PAYLOAD_SIZE = 0xFFFF;
    private static final int INITIAL_CAPACITY = 16384;

    private PacketBuffer() {
    }

    public static byte[] ensureCapacity(byte[] data, int used, int additional) {
        if (used < 0 || additional < 0 || (long) used + additional > MAX_PAYLOAD_SIZE) {
            throw new IllegalArgumentException("Packet payload exceeds the unsigned 16-bit wire limit");
        }
        if (used > (data == null ? 0 : data.length)) {
            throw new IllegalArgumentException("Packet payload length exceeds its buffer");
        }

        int required = used + additional;
        if (data != null && required <= data.length) {
            return data;
        }
        int capacity = Math.max(INITIAL_CAPACITY, data == null ? 0 : data.length * 2);
        capacity = Math.min(MAX_PAYLOAD_SIZE, Math.max(required, capacity));
        return data == null ? new byte[capacity] : Arrays.copyOf(data, capacity);
    }

    public static int utf16Size(String text) {
        return checkedSize(2L * (text == null ? 0 : text.length()) + 2);
    }

    public static int utf8Size(String text) {
        long size = 1; // Null terminator.
        if (text != null) {
            for (int i = 0; i < text.length(); i++) {
                char value = text.charAt(i);
                if (value <= 0x7F) {
                    size++;
                } else if (value <= 0x7FF) {
                    size += 2;
                } else if (Character.isHighSurrogate(value) && i + 1 < text.length()
                        && Character.isLowSurrogate(text.charAt(i + 1))) {
                    size += 4;
                    i++;
                } else if (Character.isSurrogate(value)) {
                    size++; // StandardCharsets.UTF_8 replaces an unpaired surrogate with '?'.
                } else {
                    size += 3;
                }
                checkedSize(size);
            }
        }
        return (int) size;
    }

    private static int checkedSize(long size) {
        if (size > MAX_PAYLOAD_SIZE) {
            throw new IllegalArgumentException("Packet payload exceeds the unsigned 16-bit wire limit");
        }
        return (int) size;
    }
}
