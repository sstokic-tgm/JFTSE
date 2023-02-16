package com.jftse.server.core.service;

import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;

import java.util.List;

public interface PlayerPocketService {
    PlayerPocket getItemAsPocket(Long itemPocketId, Pocket pocket);

    PlayerPocket getItemAsPocketByItemIndexAndCategoryAndPocket(Integer itemIndex, String category, Pocket pocket);

    @Deprecated(forRemoval = true)
    PlayerPocket getItemAsPocketByItemIndexAndPocket(Integer itemIndex, Pocket pocket);

    PlayerPocket findById(Long id);

    List<PlayerPocket> getPlayerPocketItems(Pocket pocket);

    int getSellPrice(PlayerPocket playerPocket);

    PlayerPocket decrementPocketItemCount(PlayerPocket playerPocket);

    PlayerPocket save(PlayerPocket playerPocket);

    void remove(Long playerPocketId);
}
