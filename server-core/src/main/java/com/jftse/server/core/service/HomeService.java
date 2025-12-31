package com.jftse.server.core.service;

import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.home.HomeInventory;
import com.jftse.entities.database.model.item.ItemHouse;
import com.jftse.entities.database.model.item.ItemHouseDeco;

import java.util.List;
import java.util.Map;

public interface HomeService {
    AccountHome save(AccountHome accountHome);

    HomeInventory save(HomeInventory homeInventory);

    HomeInventory findById(long homeInventoryId);

    AccountHome findById(Long accountHomeId);

    AccountHome findAccountHomeByAccountId(Long accountId);

    List<HomeInventory> findAllByAccountHome(AccountHome accountHome);

    ItemHouse findItemHouseByItemIndex(Integer itemIndex);

    ItemHouseDeco findItemHouseDecoByItemIndex(Integer itemIndex);

    AccountHome updateAccountHomeStatsByHomeInventory(AccountHome accountHome, HomeInventory homeInventory, boolean addition);

    AccountHome subtractStatsForRemovedHomeItems(AccountHome accountHome, Map<Integer, Integer> removedItems);

    void removeItemFromHomeInventory(Long homeInventoryId);

    void removeAllHomeItemsByAccountHome(AccountHome accountHome);
}
