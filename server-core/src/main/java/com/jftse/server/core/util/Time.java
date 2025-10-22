package com.jftse.server.core.util;

public final class Time {
    private static final long serverStartTime;

    static {
        serverStartTime = System.currentTimeMillis();
    }

    private Time() {
    }

    public static long getServerStartTime() {
        return serverStartTime;
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

    public static String getServerUptime() {
        long uptime = Time.getMSTime() / 1000;
        long hours = uptime / 3600;
        long minutes = (uptime % 3600) / 60;
        long seconds = uptime % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
