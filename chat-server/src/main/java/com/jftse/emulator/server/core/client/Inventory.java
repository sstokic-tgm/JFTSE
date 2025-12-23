package com.jftse.emulator.server.core.client;

/**
 * Interface defining inventory operations for a player.
 */
public interface Inventory {

    /**
     * Adds an item to the inventory by product index.
     *
     * @param productIndex the product index of the item
     * @param quantity the quantity of the item to add
     * @return true if the item was added successfully, false otherwise
     */
    boolean addItem(int productIndex, int quantity);

    /**
     * Adds an item to the inventory by item index and category.
     *
     * @param itemIndex the item index
     * @param category the category of the item
     * @param quantity the quantity of the item to add
     * @return true if the item was added successfully, false otherwise
     */
    boolean addItem(int itemIndex, String category, int quantity);

    /**
     * Removes an item from the inventory by item index and category.
     *
     * @param itemIndex the item index
     * @param category the category of the item
     * @param quantity the quantity of the item to remove
     * @return true if the item was removed successfully, false otherwise
     */
    boolean removeItem(int itemIndex, String category, int quantity);

    /**
     * Removes an item from the inventory by its unique ID.
     *
     * @param id the unique ID of the item
     * @param quantity the quantity of the item to remove
     * @return true if the item was removed successfully, false otherwise
     */
    boolean removeItem(Long id, int quantity);

    /**
     * Removes an item from the inventory by its unique ID.
     *
     * @param id the unique ID of the item
     * @return true if the item was removed successfully, false otherwise
     */
    boolean removeItem(Long id);
}
