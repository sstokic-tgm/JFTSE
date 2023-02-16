package com.jftse.emulator.server.core.life.item;

import com.jftse.emulator.server.core.life.item.quick.QuickItem;
import com.jftse.emulator.server.core.life.item.recipe.Recipe;
import com.jftse.emulator.server.core.life.item.special.*;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.item.ItemRecipe;
import com.jftse.entities.database.model.item.ItemSpecial;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.service.PlayerPocketService;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ItemFactory {
    private static ItemSpecial specialItemRingOfEXP;
    private static ItemSpecial specialItemRingOfGold;
    private static ItemSpecial specialItemRingOfWiseman;
    private static boolean backToRoomFromMatchplay;


    private ItemFactory() {
    }

    public ItemFactory(String initCommand) {
        InitSpecialItemsOnLogin(initCommand);
    }

    public static BaseItem getItem(long playerPocketIdOfItem, Pocket pocketOfPlayer) {
        final PlayerPocketService playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        final PlayerPocket playerPocket = playerPocketService.getItemAsPocket(playerPocketIdOfItem, pocketOfPlayer);
        if (playerPocket != null) {
            String category = playerPocket.getCategory();

            if (category.equals(EItemCategory.RECIPE.getName())) {
                ItemRecipe itemRecipe = ServiceManager.getInstance().getItemRecipeService().findByItemIndex(playerPocket.getItemIndex());
                return new Recipe(itemRecipe.getItemIndex(), itemRecipe.getName(), category);
            }
            if (category.equals(EItemCategory.SPECIAL.getName())) {
                return getSpecificSpecialItem(playerPocket);
            }
            if (category.equals(EItemCategory.QUICK.getName())) {
                return new QuickItem(playerPocket.getItemIndex());
            }
        }
        return null;
    }

    private static BaseItem getSpecificSpecialItem(PlayerPocket playerPocketItem) {
        ItemSpecial itemSpecial = ServiceManager.getInstance().getItemSpecialService().findByItemIndex(playerPocketItem.getItemIndex());

        if (itemSpecial.getItemIndex() == 6) {
            return new WingOfMemory(itemSpecial.getItemIndex(), itemSpecial.getName(), playerPocketItem.getCategory());
        }
        if (itemSpecial.getItemIndex() == 26) {
            return new CoupleRing(itemSpecial.getItemIndex(), itemSpecial.getName(), playerPocketItem.getCategory());
        }
        if (itemSpecial.getItemIndex() == 15) {
            return new TrunkSmall(itemSpecial.getItemIndex(), itemSpecial.getName(), playerPocketItem.getCategory());
        }
        if (itemSpecial.getItemIndex() == 16) {
            return new TrunkMedium(itemSpecial.getItemIndex(), itemSpecial.getName(), playerPocketItem.getCategory());
        }
        if (itemSpecial.getItemIndex() == 17) {
            return new TrunkLarge(itemSpecial.getItemIndex(), itemSpecial.getName(), playerPocketItem.getCategory());
        }

        return null;
    }

    public static ItemSpecial getSpecialItemInMemoryById(int idOfSpecialItem) {
        switch (idOfSpecialItem) {
            case 1:
                log.info("Special item: " + specialItemRingOfEXP.getName() + " from inMemory");
                return specialItemRingOfEXP;
            case 2:
                log.info("Special item: " + specialItemRingOfGold.getName() + " from inMemory");
                return specialItemRingOfGold;
            case 3:
                log.info("Special item: " + specialItemRingOfWiseman.getName() + " from inMemory");
                return specialItemRingOfWiseman;
            default:
                return null;
        }
    }

    public static void SetBackFromMatchplay(boolean backToRoomFromMatchplayToSet) {
        backToRoomFromMatchplay = backToRoomFromMatchplayToSet;
    }

    public static boolean GetFlagBackFromMatchplay() {
        return backToRoomFromMatchplay;
    }

    public void InitSpecialItemsOnLogin(String initCommand) {
        log.info("ItemFactory: " + initCommand);
        this.specialItemRingOfEXP = ServiceManager.getInstance().getItemSpecialService().findByItemIndex(1);
        this.specialItemRingOfGold = ServiceManager.getInstance().getItemSpecialService().findByItemIndex(2);
        this.specialItemRingOfWiseman = ServiceManager.getInstance().getItemSpecialService().findByItemIndex(3);
        this.backToRoomFromMatchplay = false;
    }
}
