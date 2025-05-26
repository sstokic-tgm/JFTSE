package com.jftse.server.core.util;

public enum AccountAction {
    LOGOUT(0),
    LOGIN(1),
    RELOG(2),
    DISCONNECT(3);

    private final int value;

    AccountAction(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AccountAction fromValue(int value) {
        for (AccountAction action : AccountAction.values()) {
            if (action.getValue() == value) {
                return action;
            }
        }
        return null;
    }
}
