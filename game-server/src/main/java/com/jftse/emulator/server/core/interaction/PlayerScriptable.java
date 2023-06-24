package com.jftse.emulator.server.core.interaction;

public interface PlayerScriptable {
    void giveExp(int exp);
    void giveGold(int gold);
    void giveAp(int ap);
    void giveItem(int productIndex, int quantity);
    void giveItem(int itemIndex, String category, int quantity);
    void sendGift(int productIndex, int quantity, String message);
    void sendChat(String name, String message);
    void sendChat(String name, String message, Integer chatMode);
}
