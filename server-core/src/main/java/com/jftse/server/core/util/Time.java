package com.jftse.server.core.util;

public final class Time {
    private static long serverStartTime;

    // difference between Unix epoch and Windows FILETIME epoch in milliseconds
    public static final long FILETIME_EPOCH_DIFFERENCE_MS = 11644473600000L;

    static {
        serverStartTime = System.currentTimeMillis();
    }

    private Time() {
    }

    public static long getServerStartTime() {
        return serverStartTime;
    }
    public static void resetServerStartTime() {
        serverStartTime = System.currentTimeMillis();
    }

    public static long getMSTime() {
        return System.currentTimeMillis() - getServerStartTime();
    }

    public static long getMSTimeDiff(long oldMSTime, long newMSTime) {
        return oldMSTime > newMSTime ? (Integer.MAX_VALUE - oldMSTime) + newMSTime : newMSTime - oldMSTime;
    }

    public static long getMSTimeDiffToNow(long oldMSTime) {
        return getMSTimeDiff(oldMSTime, getMSTime());
    }

    public static long getNSTime() {
        return System.nanoTime();
    }

    public static long getNSTimeDiff(long oldNSTime, long newNSTime) {
        return oldNSTime > newNSTime ? (Long.MAX_VALUE - oldNSTime) + newNSTime : newNSTime - oldNSTime;
    }

    public static long getNSTimeDiffToNow(long oldNSTime) {
        return getNSTimeDiff(oldNSTime, getNSTime());
    }

    public static long nanoToMillis(long nanoTime) {
        return nanoTime / 1_000_000;
    }

    public static String getServerUptime() {
        long uptime = Time.getMSTime() / 1000;
        long hours = uptime / 3600;
        long minutes = (uptime % 3600) / 60;
        long seconds = uptime % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static long toFileTimeUTC(long timeMs) {
        return (timeMs + FILETIME_EPOCH_DIFFERENCE_MS) * 10_000; // convert to 100-nanosecond intervals
    }

    public static long fromFileTimeUTC(long fileTime) {
        return (fileTime / 10_000) - FILETIME_EPOCH_DIFFERENCE_MS;
    }
}
