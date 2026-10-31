package com.jftse.server.core.protocol;

public final class PacketFrames {
    private static final int HEADER_SIZE = 8;

    private PacketFrames() {
    }

    public static void validate(byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("No packet frames to encode");
        }

        int offset = 0;
        while (offset < data.length) {
            int remaining = data.length - offset;
            if (remaining < HEADER_SIZE) {
                throw new IllegalArgumentException("Truncated packet header at offset " + offset);
            }
            int payloadSize = (data[offset + 6] & 0xFF) | ((data[offset + 7] & 0xFF) << 8);
            int frameSize = HEADER_SIZE + payloadSize;
            if (frameSize > remaining) {
                throw new IllegalArgumentException("Truncated packet payload at offset " + offset);
            }
            offset += frameSize;
        }
    }
}
