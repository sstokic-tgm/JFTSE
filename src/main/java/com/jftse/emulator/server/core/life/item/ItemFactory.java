package com.jftse.emulator.server.core.life.item;

import com.jftse.emulator.server.core.item.EItemCategory;
import com.jftse.emulator.server.core.life.item.quick.QuickItem;
import com.jftse.emulator.server.core.life.item.recipe.Recipe;
import com.jftse.emulator.server.core.life.item.special.*;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.service.PlayerPocketService;
import com.jftse.emulator.server.database.model.item.ItemRecipe;
import com.jftse.emulator.server.database.model.item.ItemSpecial;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.database.model.pocket.Pocket;

public class ItemFactory {

    private ItemFactory() {
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
}
