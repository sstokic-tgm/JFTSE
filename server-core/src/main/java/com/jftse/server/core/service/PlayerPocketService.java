package com.jftse.server.core.service;

import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;

import java.util.List;

public interface PlayerPocketService {
    PlayerPocket getItemAsPocket(Long itemPocketId, Pocket pocket);
    PlayerPocket getItemAsPocket(Long itemPocketId, Long pocketId);
    List<PlayerPocket> getItemsAsPocket(List<Long> itemPocketIds, Pocket pocket);
    List<PlayerPocket> getItemsAsPocketByItemIndex(List<Integer> itemIndices, Pocket pocket);

    PlayerPocket getItemAsPocketByItemIndexAndCategoryAndPocket(Integer itemIndex, String category, Pocket pocket);
    PlayerPocket getItemAsPocketByItemIndexAndCategoryAndPocket(Integer itemIndex, String category, Long pocketId);
    List<PlayerPocket> getItemsAsPocketByItemIndexListAndCategoryAndPocket(List<Integer> itemIndexList, String category, Pocket pocket);

    PlayerPocket findById(Long id);

    List<PlayerPocket> getPlayerPocketItems(Pocket pocket);

    List<PlayerPocket> getPlayerPocketItemsByCategory(Pocket pocket, String category);

    int getSellPrice(PlayerPocket playerPocket);

    PlayerPocket decrementPocketItemCount(PlayerPocket playerPocket);

    PlayerPocket save(PlayerPocket playerPocket);
    List<PlayerPocket> saveAll(List<PlayerPocket> playerPockets);

    void remove(Long playerPocketId);
}
