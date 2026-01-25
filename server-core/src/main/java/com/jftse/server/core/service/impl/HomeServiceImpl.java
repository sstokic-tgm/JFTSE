package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.home.HomeInventory;
import com.jftse.entities.database.model.item.ItemHouse;
import com.jftse.entities.database.model.item.ItemHouseDeco;
import com.jftse.entities.database.repository.home.AccountHomeRepository;
import com.jftse.entities.database.repository.home.HomeInventoryRepository;
import com.jftse.entities.database.repository.item.ItemHouseDecoRepository;
import com.jftse.entities.database.repository.item.ItemHouseRepository;
import com.jftse.server.core.item.EItemHouseDeco;
import com.jftse.server.core.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {
    private final AccountHomeRepository accountHomeRepository;
    private final HomeInventoryRepository homeInventoryRepository;
    private final ItemHouseRepository itemHouseRepository;
    private final ItemHouseDecoRepository itemHouseDecoRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AccountHome save(AccountHome accountHome) {
        return accountHomeRepository.save(accountHome);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public HomeInventory save(HomeInventory homeInventory) {
        return homeInventoryRepository.save(homeInventory);
    }

    @Override
    @Transactional(readOnly = true)
    public HomeInventory findById(long homeInventoryId) {
        Optional<HomeInventory> homeInventory = homeInventoryRepository.findById(homeInventoryId);
        return homeInventory.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountHome findById(Long accountHomeId) {
        Optional<AccountHome> accountHome = accountHomeRepository.findById(accountHomeId);
        return accountHome.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountHome findAccountHomeByAccountId(Long accountId) {

        Optional<AccountHome> accountHome = accountHomeRepository.findByAccountId(accountId);
        return accountHome.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HomeInventory> findAllByAccountHome(AccountHome accountHome) {
        return homeInventoryRepository.findAllByAccountHome(accountHome);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemHouse findItemHouseByItemIndex(Integer itemIndex) {
        Optional<ItemHouse> itemHouse = itemHouseRepository.findItemHouseByItemIndex(itemIndex);
        return itemHouse.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemHouseDeco findItemHouseDecoByItemIndex(Integer itemIndex) {
        Optional<ItemHouseDeco> itemHouseDeco = itemHouseDecoRepository.findItemHouseDecoByItemIndex(itemIndex);
        return itemHouseDeco.orElse(null);
    }

    @Override
    public AccountHome updateAccountHomeStatsByHomeInventory(AccountHome accountHome, HomeInventory homeInventory, boolean addition) {
        ItemHouse itemHouse = itemHouseRepository.findItemHouseByLevel(accountHome.getLevel()).orElse(null);
        ItemHouseDeco itemHouseDeco = itemHouseDecoRepository.findItemHouseDecoByItemIndex(homeInventory.getItemIndex()).orElse(null);

        if (itemHouse != null && itemHouseDeco != null) {
            if (addition) {
                byte basicBonusGold = (byte) (accountHome.getBasicBonusGold() + itemHouseDeco.getAddGold());
                byte basicBonusExp = (byte) (accountHome.getBasicBonusExp() + itemHouseDeco.getAddExp());
                byte battleBonusGold = (byte) (accountHome.getBattleBonusGold() + itemHouseDeco.getAddBattleGold());
                byte battleBonusExp = (byte) (accountHome.getBattleBonusExp() + itemHouseDeco.getAddBattleExp());

                accountHome.setBasicBonusGold(basicBonusGold <= itemHouse.getMaxAddPercent() ? basicBonusGold : itemHouse.getMaxAddPercent());
                accountHome.setBasicBonusExp(basicBonusExp <= itemHouse.getMaxAddPercent() ? basicBonusExp : itemHouse.getMaxAddPercent());
                accountHome.setBattleBonusGold(battleBonusGold <= itemHouse.getMaxAddPercent() ? battleBonusGold : itemHouse.getMaxAddPercent());
                accountHome.setBattleBonusExp(battleBonusExp <= itemHouse.getMaxAddPercent() ? battleBonusExp : itemHouse.getMaxAddPercent());
                accountHome.setHousingPoints(accountHome.getHousingPoints() + itemHouseDeco.getHousingPoint());

                if (itemHouseDeco.getKind().equals(EItemHouseDeco.FURNITURE.getName()))
                    accountHome.setFurnitureCount(accountHome.getFurnitureCount() + 1);
            }
            else {
                byte basicBonusGold = (byte) (accountHome.getBasicBonusGold() - itemHouseDeco.getAddGold());
                byte basicBonusExp = (byte) (accountHome.getBasicBonusExp() - itemHouseDeco.getAddExp());
                byte battleBonusGold = (byte) (accountHome.getBattleBonusGold() - itemHouseDeco.getAddBattleGold());
                byte battleBonusExp = (byte) (accountHome.getBattleBonusExp() - itemHouseDeco.getAddBattleExp());
                int housingPoints = accountHome.getHousingPoints() - itemHouseDeco.getHousingPoint();

                accountHome.setBasicBonusGold(basicBonusGold >= 0 ? basicBonusGold : 0);
                accountHome.setBasicBonusExp(basicBonusExp >= 0 ? basicBonusExp : 0);
                accountHome.setBattleBonusGold(battleBonusGold >= 0 ? battleBonusGold : 0);
                accountHome.setBattleBonusExp(battleBonusExp >= 0 ? battleBonusExp : 0);
                accountHome.setHousingPoints(Math.max(housingPoints, 0));

                if (itemHouseDeco.getKind().equals(EItemHouseDeco.FURNITURE.getName()))
                    accountHome.setFurnitureCount(accountHome.getFurnitureCount() - 1);
            }
        }
        return accountHome;
    }

    @Override
    public AccountHome subtractStatsForRemovedHomeItems(AccountHome accountHome, Map<Integer, Integer> removedItems) {
        if (removedItems == null || removedItems.isEmpty())
            return accountHome;

        ItemHouse itemHouse = itemHouseRepository.findItemHouseByLevel(accountHome.getLevel()).orElse(null);
        if (itemHouse == null)
            return accountHome;

        List<Integer> itemIndices = removedItems.keySet().stream().toList();
        List<ItemHouseDeco> decos = itemHouseDecoRepository.findAllByItemIndexIn(itemIndices);

        int maxPercent = itemHouse.getMaxAddPercent();
        for (ItemHouseDeco deco : decos) {
            Integer removeCount = removedItems.get(deco.getItemIndex());
            if (removeCount != null && removeCount > 0) {
                int basicGold = accountHome.getBasicBonusGold() - (deco.getAddGold() * removeCount);
                int basicExp  = accountHome.getBasicBonusExp()  - (deco.getAddExp() * removeCount);
                int battleGold = accountHome.getBattleBonusGold() - (deco.getAddBattleGold() * removeCount);
                int battleExp  = accountHome.getBattleBonusExp()  - (deco.getAddBattleExp() * removeCount);

                int housingPoints = accountHome.getHousingPoints() - (deco.getHousingPoint() * removeCount);

                accountHome.setBasicBonusGold((byte) clamp(basicGold, 0, maxPercent));
                accountHome.setBasicBonusExp((byte) clamp(basicExp, 0, maxPercent));
                accountHome.setBattleBonusGold((byte) clamp(battleGold, 0, maxPercent));
                accountHome.setBattleBonusExp((byte) clamp(battleExp, 0, maxPercent));

                accountHome.setHousingPoints(Math.max(housingPoints, 0));

                if (EItemHouseDeco.FURNITURE.getName().equals(deco.getKind())) {
                    accountHome.setFurnitureCount(Math.max(accountHome.getFurnitureCount() - removeCount, 0));
                }
            }
        }
        return accountHome;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    @Transactional
    public void removeItemFromHomeInventory(Long homeInventoryId) {
        homeInventoryRepository.deleteById(homeInventoryId);
    }

    @Override
    @Transactional
    public void removeAllHomeItemsByAccountHome(AccountHome accountHome) {
        homeInventoryRepository.deleteAllByAccountHome(accountHome);
    }
}
