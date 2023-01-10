package com.jftse.server.core.service;

import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.home.HomeInventory;
import com.jftse.entities.database.model.item.ItemHouse;
import com.jftse.entities.database.model.item.ItemHouseDeco;

import java.util.List;

public interface HomeService {
    AccountHome save(AccountHome accountHome);

    HomeInventory save(HomeInventory homeInventory);

    HomeInventory findById(long homeInventoryId);

    AccountHome findById(Long accountHomeId);

    AccountHome findAccountHomeByAccountId(Long accountId);

    List<HomeInventory> findAllByAccountHome(AccountHome accountHome);

    ItemHouse findItemHouseByItemIndex(Integer itemIndex);

    ItemHouseDeco findItemHouseDecoByItemIndex(Integer itemIndex);

    void updateAccountHomeStatsByHomeInventory(AccountHome accountHome, HomeInventory homeInventory, boolean addition);

    void removeItemFromHomeInventory(Long homeInventoryId);
}
