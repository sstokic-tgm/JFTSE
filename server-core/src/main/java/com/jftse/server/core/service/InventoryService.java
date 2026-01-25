package com.jftse.server.core.service;

import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.item.AddItemHook;

import java.util.List;
import java.util.function.Consumer;

public interface InventoryService {
    /**
     * Adds an item to the inventory by product index.
     *
     * @param productIndex the product index of the item
     * @param quantity the quantity of the item to add
     * @param postActions optional actions to be executed after adding the item
     * @return a not empty list of PlayerPocket if the item was added successfully, empty list otherwise
     */
    List<PlayerPocket> addItem(long playerId, int productIndex, int quantity, List<Consumer<? super AddItemHook>> postActions);

    /**
     * Adds an item to the inventory by item index and category.
     *
     * @param itemIndex the item index
     * @param category the category of the item
     * @param quantity the quantity of the item to add
     * @param postActions optional actions to be executed after adding the item
     * @return a not empty list of PlayerPocket if the item was added successfully, empty list otherwise
     */
    List<PlayerPocket> addItem(long playerId, int itemIndex, String category, int quantity, List<Consumer<? super AddItemHook>> postActions);

    /**
     * Removes an item from the inventory by item index and category.
     *
     * @param itemIndex the item index
     * @param category the category of the item
     * @param quantity the quantity of the item to remove
     * @return true if the item was removed successfully, false otherwise
     */
    boolean removeItem(long playerId, int itemIndex, String category, int quantity);

    /**
     * Removes an item from the inventory by its unique ID.
     *
     * @param id the unique ID of the item
     * @param quantity the quantity of the item to remove
     * @return true if the item was removed successfully, false otherwise
     */
    boolean removeItem(long playerId, Long id, int quantity);

    /**
     * Removes an item from the inventory by its unique ID.
     *
     * @param id the unique ID of the item
     * @return true if the item was removed successfully, false otherwise
     */
    boolean removeItem(long playerId, Long id);
}
