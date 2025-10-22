package com.jftse.server.core.util;

import lombok.Getter;

import java.time.*;

public final class GameTime {
    @Getter private static Instant gameTime;
    private static long gameMsTime;

    private static Instant gameTimeSystemPoint;
    private static Instant gameTimeSteadyPoint;

    private static LocalDateTime dateTime;

    @Getter private static final Instant startTime;

    static {
        updateGameTimers();
        startTime = Instant.now();
    }

    private GameTime() {
    }

    public static void updateGameTimers() {
        gameTime = Instant.now();
        gameMsTime = System.currentTimeMillis();
        gameTimeSystemPoint = Instant.now();
        gameTimeSteadyPoint = Instant.now();

        dateTime = LocalDateTime.ofInstant(gameTime, ZoneId.systemDefault());
    }

    public static LocalDateTime getDateAndTime(){
        return dateTime;
    }

    public static long getUptimeSeconds() {
        return Duration.between(startTime, gameTime).toSeconds();
    }

    public static <T extends Clock> Instant getTime(Class<T> clockClass) {
        if (clockClass == Clock.systemUTC().getClass())
            return getSystemTime();
        else
            return now();
    }

    public static Instant now() {
        return gameTimeSteadyPoint;
    }

    public static Instant getSystemTime() {
        return gameTimeSystemPoint;
    }

    public static long getGameTimeMS() {
        return gameMsTime;
    }
}
