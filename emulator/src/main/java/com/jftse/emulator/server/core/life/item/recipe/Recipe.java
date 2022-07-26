package com.jftse.emulator.server.core.life.item.recipe;

import com.jftse.emulator.common.utilities.StringTokenizer;
import com.jftse.emulator.server.core.item.EItemCategory;
import com.jftse.emulator.server.core.item.EItemUseType;
import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.service.*;
import com.jftse.entities.database.model.item.ItemRecipe;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;

import java.util.*;

public class Recipe extends BaseItem {
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final ProductService productService;
    private final PlayerService playerService;

    private final ItemRecipe itemRecipe;
    private final List<PlayerPocket> resultPlayerPocketList;
    private final List<PlayerPocket> itemsToUpdateFromClient;
    private final List<Long> itemsToRemoveFromClient;

    public Recipe(int itemIndex, String name, String category) {
        super(itemIndex, name, category);

        pocketService = ServiceManager.getInstance().getPocketService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        productService = ServiceManager.getInstance().getProductService();
        playerService = ServiceManager.getInstance().getPlayerService();

        itemRecipe = ServiceManager.getInstance().getItemRecipeService().findByItemIndex(itemIndex);

        resultPlayerPocketList = new ArrayList<>();
        itemsToUpdateFromClient = new ArrayList<>();
        itemsToRemoveFromClient = new ArrayList<>();
    }

    @Override
    public boolean processPlayer(Player player) {
        player = playerService.findById(player.getId());

        if (itemRecipe == null || player == null)
            return false;

        int requireGold = itemRecipe.getRequireGold();
        int playerGold = player.getGold();
        if (playerGold < requireGold)
            return false;

        int newGold = player.getGold() - requireGold;
        newGold = Math.max(newGold, 0);

        player = playerService.setMoney(player, newGold);

        return true;
    }

    @Override
    public boolean processPocket(Pocket pocket) {
        if (itemRecipe == null)
            return false;

        pocket = pocketService.findById(pocket.getId());
        if (pocket == null)
            return false;

        PlayerPocket playerPocketRecipe = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(this.getItemIndex(), this.getCategory(), pocket);
        if (playerPocketRecipe == null)
            return false;

        // get needed materials
        StringTokenizer st = new StringTokenizer(itemRecipe.getMaterials(), ";");
        List<String> materials = st.get();
        List<Map<String, Integer>> neededMaterialsList = new ArrayList<>();
        for (String neededMaterial : materials) {
            HashMap<String, Integer> data = new HashMap<>(2);
            st = new StringTokenizer(neededMaterial, "=");
            List<String> tmpList = st.get();
            data.put("material", Integer.parseInt(tmpList.get(0)));
            data.put("count", Integer.parseInt(tmpList.get(1)));
            neededMaterialsList.add(data);
        }
        neededMaterialsList.removeIf(map -> map.get("material") == 0);

        // check if needed materials are present in the pocket
        boolean hasNeededMaterials = false;
        for (Map<String, Integer> neededMaterialsMap : neededMaterialsList) {
            Integer material = neededMaterialsMap.get("material");
            Integer count = neededMaterialsMap.get("count");

            PlayerPocket ppMaterial = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(material, EItemCategory.MATERIAL.getName(), pocket);
            if (ppMaterial == null)
                break;

            if (ppMaterial.getItemCount() < count) {
                hasNeededMaterials = false;
                break;
            }
            hasNeededMaterials = true;
        }

        // if not enough materials
        if (!hasNeededMaterials)
            return false;

        // get all possible mutations
        st = new StringTokenizer(itemRecipe.getMutations(), ";");
        List<String> mutations = st.get();
        List<Map<String, Integer>> mutationsList = new ArrayList<>();
        for (String mutation : mutations) {
            HashMap<String, Integer> data = new HashMap<>(4);
            st = new StringTokenizer(mutation, "=");
            List<String> tmpList = st.get();

            data.put("mutation", Integer.parseInt(tmpList.get(0)));

            st = new StringTokenizer(tmpList.get(1), ",");
            tmpList = new ArrayList<>(st.get());

            data.put("probability", Integer.parseInt(tmpList.get(0)));
            data.put("min", Integer.parseInt(tmpList.get(1)));
            data.put("max", Integer.parseInt(tmpList.get(2)));
            mutationsList.add(data);
        }
        mutationsList.removeIf(map -> map.get("mutation") == 0);

        // pick a random mutation between 0-100(inclusive) based on it's probability
        Random rnd = new Random();
        int number = rnd.nextInt(101);
        int end = 0;

        Map<String, Integer> drawnRecipe = null;
        for (Map<String, Integer> dataMap : mutationsList) {
            int probability = dataMap.get("probability");
            end += probability;

            if (end >= number) {
                drawnRecipe = dataMap;
                break;
            }
        }
        if (drawnRecipe == null)
            return false;

        Product product = productService.findProductByProductItemIndex(drawnRecipe.get("mutation"));
        if (product == null)
            return false;

        // save picked up mutation
        PlayerPocket ppMutation = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(product.getItem0(), product.getCategory(), pocket);
        int existingItemCount = 0;
        boolean existingItem = false;

        if (ppMutation != null && !ppMutation.getUseType().equals("N/A")) {
            existingItemCount = ppMutation.getItemCount();
            existingItem = true;
        } else {
            ppMutation = new PlayerPocket();
        }

        if (!existingItem)
            ppMutation.setPocket(pocket);

        ppMutation.setCategory(product.getCategory());
        ppMutation.setItemIndex(product.getItem0());
        ppMutation.setUseType(product.getUseType());

        int min = drawnRecipe.get("min");
        int max = drawnRecipe.get("max");
        int drawnCountAmount = rnd.nextInt(max - min + 1) + min;

        ppMutation.setItemCount(drawnCountAmount);
        // no idea how itemCount can be null here, but ok
        ppMutation.setItemCount((ppMutation.getItemCount() == null ? 0 : ppMutation.getItemCount()) + existingItemCount);

        if (ppMutation.getUseType().equalsIgnoreCase(EItemUseType.TIME.getName())) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.add(Calendar.DAY_OF_MONTH, ppMutation.getItemCount());

            ppMutation.setCreated(cal.getTime());
        }

        ppMutation = playerPocketService.save(ppMutation);
        if (!existingItem)
            pocket = pocketService.incrementPocketBelongings(pocket);

        // remove needed materials
        for (Map<String, Integer> neededMaterialsMap : neededMaterialsList) {
            Integer material = neededMaterialsMap.get("material");
            Integer count = neededMaterialsMap.get("count");

            PlayerPocket ppMaterial = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(material, EItemCategory.MATERIAL.getName(), pocket);
            if (ppMaterial != null) {
                int newItemCount = ppMaterial.getItemCount() - count;
                if (newItemCount <= 0) {
                    playerPocketService.remove(ppMaterial.getId());
                    pocket = pocketService.decrementPocketBelongings(pocket);

                    itemsToRemoveFromClient.add(ppMaterial.getId());
                } else {
                    ppMaterial.setItemCount(newItemCount);
                    ppMaterial = playerPocketService.save(ppMaterial);

                    itemsToUpdateFromClient.add(ppMaterial);
                }
            }
        }

        int newItemCount = playerPocketRecipe.getItemCount() - 1;
        if (newItemCount <= 0) {
            playerPocketService.remove(playerPocketRecipe.getId());
            pocket = pocketService.decrementPocketBelongings(pocket);

            itemsToRemoveFromClient.add(playerPocketRecipe.getId());
        } else {
            playerPocketRecipe.setItemCount(newItemCount);
            playerPocketRecipe = playerPocketService.save(playerPocketRecipe);

            itemsToUpdateFromClient.add(playerPocketRecipe);
        }

        // add item to result
        resultPlayerPocketList.add(ppMutation);
        if (existingItem)
            itemsToUpdateFromClient.add(ppMutation);

        return true;
    }

    public List<PlayerPocket> getResult() {
        return resultPlayerPocketList;
    }

    public List<Long> getItemsToRemoveFromClient() {
        return itemsToRemoveFromClient;
    }

    public List<PlayerPocket> getItemsToUpdateFromClient() {
        return itemsToUpdateFromClient;
    }
}
