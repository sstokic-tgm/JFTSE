package com.jftse.emulator.server.core.interaction;

/**
 * Interface defining scriptable interactions with a player.
 */
public interface PlayerScriptable {
    /**
     * Gives experience points to the player.
     *
     * @param exp the amount of experience points to give
     */
    void giveExp(int exp);

    /**
     * Gives gold to the player.
     *
     * @param gold the amount of gold to give
     */
    void giveGold(int gold);

    /**
     * Gives ability points to the player.
     *
     * @param ap the amount of ability points to give
     */
    void giveAp(int ap);

    /**
     * Gives an item to the player by product index.
     *
     * @param productIndex the product index of the item
     * @param quantity the quantity of the item to give
     */
    void giveItem(int productIndex, int quantity);

    /**
     * Gives an item to the player by item index and category.
     *
     * @param itemIndex the item index
     * @param category the category of the item
     * @param quantity the quantity of the item to give
     */
    void giveItem(int itemIndex, String category, int quantity);

    /**
     * Sends a gift to another player.
     *
     * @param productIndex the product index of the gift item
     * @param quantity the quantity of the gift item
     * @param message an optional message to include with the gift
     */
    void sendGift(int productIndex, int quantity, String message);

    /**
     * Sends a message to the player.
     *
     * @param message the message content
     */
    void sendMessage(String message);

    /**
     * Sends a message to the player from a specific sender.
     *
     * @param sender the name of the sender
     * @param message the message content
     */
    void sendMessage(String sender, String message);

    /**
     * Sends a chat message to the player.
     *
     * @param name the name of the sender
     * @param message the chat message content
     */
    void sendChat(String name, String message);

    /**
     * Sends a chat message to the player with a specific chat mode.
     *
     * @param name the name of the sender
     * @param message the chat message content
     * @param chatMode the chat mode (e.g., normal, whisper, shout)
     */
    void sendChat(String name, String message, Integer chatMode);
}
