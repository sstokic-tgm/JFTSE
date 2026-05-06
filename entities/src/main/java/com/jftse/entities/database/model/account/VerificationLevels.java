package com.jftse.entities.database.model.account;

public enum VerificationLevels {
    NONE(0),
    EMAIL(1),
    DISCORD(2),
    EMAIL_AND_DISCORD(3);

    private final int level;

    VerificationLevels(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public static VerificationLevels fromLevel(int level) {
        for (VerificationLevels v : values()) {
            if (v.getLevel() == level) {
                return v;
            }
        }
        return NONE;
    }
}
