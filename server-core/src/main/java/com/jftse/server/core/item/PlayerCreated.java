package com.jftse.server.core.item;

import com.jftse.entities.database.model.player.Player;

public record PlayerCreated(Player player) implements AddItemHook {
}
