package com.jftse.emulator.server.core.rabbit;

import lombok.Getter;

@Getter
public enum MessageTypes {
    GUILD_MEMBER_LIST_ON_DISCONNECT("GUILD_MEMBER_LIST_ON_DISCONNECT"),
    GUILD_MEMBER_LIST_ON_REQUEST("GUILD_MEMBER_LIST_ON_REQUEST"),
    REFRESH_FRIEND_LIST("REFRESH_FRIEND_LIST"),
    REFRESH_FRIEND_RELATION("REFRESH_FRIEND_RELATION"),
    UPDATE_PLAYER_MONEY("UPDATE_PLAYER_MONEY"),
    CHAT_WHISPER("CHAT_WHISPER");

    private final String value;

    MessageTypes(String value) {
        this.value = value;
    }

    public static MessageTypes fromString(String type) {
        for (MessageTypes messageType : MessageTypes.values()) {
            if (messageType.getValue().equalsIgnoreCase(type)) {
                return messageType;
            }
        }
        throw new IllegalArgumentException("No enum constant for type: " + type);
    }

    public static MessageTypes fromStringOrDefault(String type, MessageTypes defaultType) {
        for (MessageTypes messageType : MessageTypes.values()) {
            if (messageType.getValue().equalsIgnoreCase(type)) {
                return messageType;
            }
        }
        return defaultType;
    }
}
