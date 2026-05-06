package com.jftse.emulator.server.core.utils;

import java.util.Random;

public class ServingPositionGenerator {
    public static final int LEFT_X_MIN = 0;
    public static final int LEFT_X_MAX = 3;
    public static final int MIDDLE_X = 4;
    public static final int RIGHT_X_MIN = 5;
    public static final int RIGHT_X_MAX = 6;

    public static final int LEFT_Y_MIN = 0;
    public static final int LEFT_Y_MAX = 38;
    public static final int MIDDLE_Y_MIN = 40;
    public static final int MIDDLE_Y_MAX = 78;
    public static final int RIGHT_Y_MIN = 79;
    public static final int RIGHT_Y_MAX = 255;

    private static final Random random = new Random();

    public static int randomServingPositionXOffset() {
        return random.nextInt(RIGHT_X_MAX + 1);
    }

    public static int randomServingPositionYOffset(int servingPositionXOffset) {
        int servingPositionYOffset;
        if (servingPositionXOffset >= LEFT_X_MIN && servingPositionXOffset <= LEFT_X_MAX) {
            servingPositionYOffset = random.nextBoolean() ? MIDDLE_Y_MIN + random.nextInt(MIDDLE_Y_MAX - MIDDLE_Y_MIN + 1) : RIGHT_Y_MIN + random.nextInt(RIGHT_Y_MAX - RIGHT_Y_MIN + 1);
        } else {
            servingPositionYOffset = random.nextBoolean() ? LEFT_Y_MIN + random.nextInt(LEFT_Y_MAX - LEFT_Y_MIN + 1) : MIDDLE_Y_MIN + random.nextInt(MIDDLE_Y_MAX - MIDDLE_Y_MIN + 1);
        }

        return servingPositionYOffset;
    }
}
