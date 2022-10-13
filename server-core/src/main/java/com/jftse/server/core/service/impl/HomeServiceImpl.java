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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class HomeServiceImpl implements HomeService {
    private final AccountHomeRepository accountHomeRepository;
    private final HomeInventoryRepository homeInventoryRepository;
    private final ItemHouseRepository itemHouseRepository;
    private final ItemHouseDecoRepository itemHouseDecoRepository;

    @Override
    public AccountHome save(AccountHome accountHome) {
        return accountHomeRepository.save(accountHome);
    }

    @Override
    public HomeInventory save(HomeInventory homeInventory) {
        return homeInventoryRepository.save(homeInventory);
    }

    @Override
    public HomeInventory findById(long homeInventoryId) {
        Optional<HomeInventory> homeInventory = homeInventoryRepository.findById(homeInventoryId);
        return homeInventory.orElse(null);
    }

    @Override
    public AccountHome findById(Long accountHomeId) {
        Optional<AccountHome> accountHome = accountHomeRepository.findById(accountHomeId);
        return accountHome.orElse(null);
    }

    @Override
    public AccountHome findAccountHomeByAccountId(Long accountId) {

        Optional<AccountHome> accountHome = accountHomeRepository.findAccountHomeByAccountId(accountId);
        return accountHome.orElse(null);
    }

    @Override
    public List<HomeInventory> findAllByAccountHome(AccountHome accountHome) {
        return homeInventoryRepository.findAllByAccountHome(accountHome);
    }

    @Override
    public ItemHouse findItemHouseByItemIndex(Integer itemIndex) {
        Optional<ItemHouse> itemHouse = itemHouseRepository.findItemHouseByItemIndex(itemIndex);
        return itemHouse.orElse(null);
    }

    @Override
    public ItemHouseDeco findItemHouseDecoByItemIndex(Integer itemIndex) {
        Optional<ItemHouseDeco> itemHouseDeco = itemHouseDecoRepository.findItemHouseDecoByItemIndex(itemIndex);
        return itemHouseDeco.orElse(null);
    }

    @Override
    public void updateAccountHomeStatsByHomeInventory(AccountHome accountHome, HomeInventory homeInventory, boolean addition) {
        ItemHouse itemHouse = itemHouseRepository.findItemHouseByLevel(accountHome.getLevel()).orElse(null);
        ItemHouseDeco itemHouseDeco = itemHouseDecoRepository.findItemHouseDecoByItemIndex(homeInventory.getItemIndex()).orElse(null);

        if (itemHouse != null && itemHouseDeco != null) {
            if (addition) {
                byte basicBonusGold = (byte) (accountHome.getBasicBonusGold() + itemHouseDeco.getAddGold());
                byte basicBonusExp = (byte) (accountHome.getBasicBonusExp() + itemHouseDeco.getAddExp());
                byte battleBonusGold = (byte) (accountHome.getBattleBonusGold() + itemHouseDeco.getAddBattleGold());
                byte battleBonusExp = (byte) (accountHome.getBattleBonusGold() + itemHouseDeco.getAddBattleExp());

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
                byte battleBonusExp = (byte) (accountHome.getBattleBonusGold() - itemHouseDeco.getAddBattleExp());
                int housingPoints = accountHome.getHousingPoints() - itemHouseDeco.getHousingPoint();

                accountHome.setBasicBonusGold(basicBonusGold >= 0 ? basicBonusGold : 0);
                accountHome.setBasicBonusExp(basicBonusExp >= 0 ? basicBonusExp : 0);
                accountHome.setBattleBonusGold(battleBonusGold >= 0 ? battleBonusGold : 0);
                accountHome.setBattleBonusExp(battleBonusExp >= 0 ? battleBonusExp : 0);
                accountHome.setHousingPoints(Math.max(housingPoints, 0));

                if (itemHouseDeco.getKind().equals(EItemHouseDeco.FURNITURE.getName()))
                    accountHome.setFurnitureCount(accountHome.getFurnitureCount() - 1);
            }

            save(accountHome);
        }
    }

    @Override
    public void removeItemFromHomeInventory(Long homeInventoryId) {
        homeInventoryRepository.deleteById(homeInventoryId);
    }
}
