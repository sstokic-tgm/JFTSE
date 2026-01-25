package com.jftse.emulator.server.core.life.item;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.server.core.protocol.IPacket;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public abstract class BaseItem {
    private final int itemIndex;
    private final String name;
    private final String category;

    protected Long localPlayerId;
    protected final MultiValueMap<Long, IPacket> packetsToSend;

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

    public abstract boolean processPlayer(FTPlayer player);
    public abstract boolean processPocket(Long pocketId);

    public final MultiValueMap<Long, IPacket> getPacketsToSend() {
        return packetsToSend;
    }
}
