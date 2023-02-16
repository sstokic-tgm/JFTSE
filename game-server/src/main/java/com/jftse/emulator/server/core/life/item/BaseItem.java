package com.jftse.emulator.server.core.life.item;

import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.protocol.Packet;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public abstract class BaseItem {
    private final int itemIndex;
    private final String name;
    private final String category;

    protected Long localPlayerId;
    protected final MultiValueMap<Long, Packet> packetsToSend;

    protected BaseItem(final int itemIndex, final String name, final String category) {
        this.itemIndex = itemIndex;
        this.name = name;
        this.category = category;

        this.packetsToSend = new LinkedMultiValueMap<>();
    }

    public final int getItemIndex() {
        return itemIndex;
    }

    public final String getName() {
        return name;
    }

    public final String getCategory() {
        return category;
    }

    public abstract boolean processPlayer(Player player);
    public abstract boolean processPocket(Pocket pocket);

    public final MultiValueMap<Long, Packet> getPacketsToSend() {
        return packetsToSend;
    }
}
