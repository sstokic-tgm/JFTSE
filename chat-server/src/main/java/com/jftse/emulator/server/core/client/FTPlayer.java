package com.jftse.emulator.server.core.client;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.player.Player;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FTPlayer {
    private final ServiceManager serviceManager;
    private Player player;
    private Inventory inventory;

    private FTPlayer() {
        this.serviceManager = ServiceManager.getInstance();
    }

    public FTPlayer(Long playerId) throws ValidationException {
        this();
        this.player = serviceManager.getPlayerService().findById(playerId);
        if (player == null)
            throw new ValidationException("Player not found");

        this.inventory = new InventoryImpl(this.player, this.serviceManager);
    }


}
