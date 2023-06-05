package com.jftse.emulator.server.core.client;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.item.ItemHouse;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.item.*;
import org.springframework.util.ReflectionUtils;

import java.util.*;

public class InventoryImpl implements Inventory {
    private final ServiceManager serviceManager;
    private final Player player;

    public InventoryImpl(Player player, ServiceManager serviceManager) {
        this.player = player;
        this.serviceManager = serviceManager;
    }

    @Override
    public boolean addItem(int productIndex, int quantity) {
        Product product = serviceManager.getProductService().findProductByProductItemIndex(productIndex);
        if (product == null)
            return false;
        return addItem(product, quantity);
    }


    @Override
    public boolean addItem(int itemIndex, String category, int quantity) {
        Product product = serviceManager.getProductService().findProductByItemAndCategory(itemIndex, category);
        if (product == null)
            return false;
        return addItem(product, quantity);
    }

    private boolean addItem(Product product, int quantity) {
        if (product.getCategory().equals(EItemCategory.PET_CHAR.getName())) {
            return false;
        }

        List<PlayerPocket> playerPocketList = new ArrayList<>();

        if (!product.getCategory().equals(EItemCategory.CHAR.getName())) {
            if (product.getCategory().equals(EItemCategory.HOUSE.getName())) {
                ItemHouse itemHouse = serviceManager.getHomeService().findItemHouseByItemIndex(product.getItem0());
                AccountHome accountHome = serviceManager.getHomeService().findAccountHomeByAccountId(this.player.getAccount().getId());

                accountHome.setLevel(itemHouse.getLevel());
                accountHome = serviceManager.getHomeService().save(accountHome);
            } else {
                Pocket pocket = serviceManager.getPocketService().findById(this.player.getPocket().getId());

                if (product.getItem1() != 0) {

                    List<Integer> itemPartList = new ArrayList<>();

                    // use reflection to get indexes of item0-9
                    ReflectionUtils.doWithFields(product.getClass(), field -> {

                        if (field.getName().startsWith("item")) {

                            field.setAccessible(true);

                            Integer itemIndex = (Integer) field.get(product);
                            if (itemIndex != 0) {
                                itemPartList.add(itemIndex);
                            }

                            field.setAccessible(false);
                        }
                    });

                    if (product.getForPlayer() != -1) {

                        Player newPlayer = serviceManager.getProductService().createNewPlayer(this.player.getAccount(), product.getForPlayer());
                        Pocket newPlayerPocket = serviceManager.getPocketService().findById(newPlayer.getPocket().getId());

                        for (Integer itemIndex : itemPartList) {
                            PlayerPocket playerPocket = new PlayerPocket();
                            playerPocket.setCategory(product.getCategory());
                            playerPocket.setItemIndex(itemIndex);
                            playerPocket.setUseType(product.getUseType());
                            playerPocket.setItemCount(1);
                            playerPocket.setPocket(newPlayerPocket);

                            serviceManager.getPlayerPocketService().save(playerPocket);
                            newPlayerPocket = serviceManager.getPocketService().incrementPocketBelongings(newPlayerPocket);
                        }
                    } else {
                        for (Integer itemIndex : itemPartList) {

                            PlayerPocket playerPocket = new PlayerPocket();
                            playerPocket.setCategory(product.getCategory());
                            playerPocket.setItemIndex(itemIndex);
                            playerPocket.setUseType(product.getUseType());
                            playerPocket.setItemCount(1);
                            playerPocket.setPocket(pocket);

                            playerPocket = serviceManager.getPlayerPocketService().save(playerPocket);
                            pocket = serviceManager.getPocketService().incrementPocketBelongings(pocket);

                            playerPocketList.add(playerPocket);
                        }
                    }
                } else {
                    PlayerPocket playerPocket = serviceManager.getPlayerPocketService().getItemAsPocketByItemIndexAndCategoryAndPocket(product.getItem0(), product.getCategory(), player.getPocket());
                    int existingItemCount = 0;
                    boolean existingItem = false;

                    if (playerPocket != null && !playerPocket.getUseType().equals("N/A")) {
                        existingItemCount = playerPocket.getItemCount();
                        existingItem = true;
                    } else {
                        playerPocket = new PlayerPocket();
                    }

                    playerPocket.setCategory(product.getCategory());
                    playerPocket.setItemIndex(product.getItem0());
                    playerPocket.setUseType(product.getUseType());

                    if (playerPocket.getUseType().equals("N/A") && quantity > 1 && existingItemCount == 0)
                        quantity = 1;

                    playerPocket.setItemCount(quantity + existingItemCount);

                    if (playerPocket.getUseType().equalsIgnoreCase(EItemUseType.TIME.getName())) {
                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        cal.add(Calendar.DAY_OF_MONTH, playerPocket.getItemCount());

                        playerPocket.setCreated(cal.getTime());
                    }
                    playerPocket.setPocket(pocket);

                    playerPocket = serviceManager.getPlayerPocketService().save(playerPocket);
                    if (!existingItem)
                        pocket = serviceManager.getPocketService().incrementPocketBelongings(pocket);

                    playerPocketList.add(playerPocket);
                }
                player.setPocket(pocket);
            }
        } else {
            serviceManager.getProductService().createNewPlayer(this.player.getAccount(), product.getForPlayer());
        }
        return true;
    }

    @Override
    public boolean removeItem(int itemIndex, String category, int quantity) {
        PlayerPocket playerPocket = serviceManager.getPlayerPocketService().getItemAsPocketByItemIndexAndCategoryAndPocket(itemIndex, category, player.getPocket());
        if (playerPocket == null)
            return false;
        removeItem(playerPocket.getId(), quantity);
        return false;
    }

    @Override
    public boolean removeItem(Long id, int quantity) {
        PlayerPocket playerPocket = serviceManager.getPlayerPocketService().getItemAsPocket(id, this.player.getPocket());
        if (playerPocket != null) {
            if (playerPocket.getItemCount() > quantity) {
                playerPocket.setItemCount(playerPocket.getItemCount() - quantity);
                serviceManager.getPlayerPocketService().save(playerPocket);
                return true;
            } else if (playerPocket.getItemCount() == quantity) {
                return removeItem(id);
            }
        }
        return false;
    }

    @Override
    public boolean removeItem(Long id) {
        PlayerPocket playerPocket = serviceManager.getPlayerPocketService().findById(id);
        if (playerPocket == null)
            return false;

        serviceManager.getPocketService().decrementPocketBelongings(playerPocket.getPocket());
        serviceManager.getPlayerPocketService().remove(playerPocket.getId());
        return true;
    }
}