package com.jftse.emulator.server.core.client;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.player.Player;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a player in the game with associated services and inventory.
 * Provides methods to access player data and manage inventory.
 *
 * Usage:
 * <pre>
 *     FTPlayer ftPlayer = new FTPlayer(playerId);
 *     Player playerData = ftPlayer.getPlayer();
 *     Inventory playerInventory = ftPlayer.getInventory();
 * </pre>
 */
@Getter
@Setter
public class FTPlayer {
    private final ServiceManager serviceManager;
    private Player player;
    private Inventory inventory;

    private FTPlayer() {
        this.serviceManager = ServiceManager.getInstance();
    }

    /**
     * Constructs an FTPlayer instance for the given player ID.
     *
     * @param playerId the ID of the player
     * @throws ValidationException if the player is not found
     */
    public FTPlayer(Long playerId) throws ValidationException {
        this();
        this.player = serviceManager.getPlayerService().findById(playerId);
        if (player == null)
            throw new ValidationException("Player not found");

        this.inventory = new InventoryImpl(this.player, this.serviceManager);
    }


}
