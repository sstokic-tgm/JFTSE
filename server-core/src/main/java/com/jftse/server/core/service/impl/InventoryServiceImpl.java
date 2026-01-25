package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.item.ItemHouse;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.pet.Pet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.item.*;
import com.jftse.server.core.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final ProductService productService;
    private final PlayerService playerService;
    private final HomeService homeService;
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final PetService petService;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<PlayerPocket> addItem(long playerId, int productIndex, int quantity, List<Consumer<? super AddItemHook>> postActions) {
        Product product = productService.findProductByProductItemIndex(productIndex);
        if (product == null)
            return new ArrayList<>();
        return addItem(playerId, product, quantity, postActions);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<PlayerPocket> addItem(long playerId, int itemIndex, String category, int quantity, List<Consumer<? super AddItemHook>> postActions) {
        Product product = productService.findProductByItemAndCategory(itemIndex, category);
        if (product == null)
            return new ArrayList<>();
        return addItem(playerId, product, quantity, postActions);
    }

    /**
     * Adds an item to the player's inventory based on the provided product and quantity.
     * Handles different item categories and updates the player's pocket accordingly.
     *
     * @param product the product to be added
     * @param quantity the quantity of the product to be added
     * @param postActions optional actions to be executed after adding the item
     * @return a not empty list of PlayerPocket if the item was added successfully, empty list otherwise
     */
    private List<PlayerPocket> addItem(long playerId, Product product, int quantity, List<Consumer<? super AddItemHook>> postActions) {
        List<PlayerPocket> playerPocketList = new ArrayList<>();

        Player player = playerService.findWithPocketById(playerId);
        if (player == null) {
            return playerPocketList;
        }

        if (product.getCategory().equals(EItemCategory.PET_CHAR.getName())) {
            Pet pet = petService.createPet(product.getItem0(), player);
            if (pet != null) {
                emit(postActions, new PetCreated(pet));
            }
        }

        if (!product.getCategory().equals(EItemCategory.CHAR.getName())) {
            if (product.getCategory().equals(EItemCategory.HOUSE.getName())) {
                ItemHouse itemHouse = homeService.findItemHouseByItemIndex(product.getItem0());
                AccountHome accountHome = homeService.findAccountHomeByAccountId(player.getAccount().getId());

                accountHome.setLevel(itemHouse.getLevel());
                homeService.save(accountHome);

                return playerPocketList;
            }

            if (product.getGoldBack() != 0) {
                emit(postActions, new GoldBackAdded(product.getGoldBack()));
            }

            Pocket pocket = player.getPocket();

            if (product.getItem1() != 0) {

                List<Integer> itemPartList = List.of(
                        product.getItem0(), product.getItem1(), product.getItem2(), product.getItem3(),
                        product.getItem4(), product.getItem5(), product.getItem6(), product.getItem7(),
                        product.getItem8(), product.getItem9()
                );

                if (product.getForPlayer() != -1) {

                    Player newPlayer = productService.createNewPlayer(player.getAccount(), product.getForPlayer());
                    Pocket newPlayerPocket = pocketService.findById(newPlayer.getPocket().getId());
                    emit(postActions, new PlayerCreated(newPlayer));

                    for (Integer itemIndex : itemPartList) {
                        PlayerPocket playerPocket = new PlayerPocket();
                        playerPocket.setCategory(product.getCategory());
                        playerPocket.setItemIndex(itemIndex);
                        playerPocket.setUseType(product.getUseType());
                        playerPocket.setItemCount(1);
                        playerPocket.setPocket(newPlayerPocket);

                        playerPocketService.save(playerPocket);
                        newPlayerPocket = pocketService.incrementPocketBelongings(newPlayerPocket);
                    }

                    return playerPocketList;
                }

                for (Integer itemIndex : itemPartList) {
                    PlayerPocket playerPocket = new PlayerPocket();
                    playerPocket.setCategory(product.getCategory());
                    playerPocket.setItemIndex(itemIndex);
                    playerPocket.setUseType(product.getUseType());
                    playerPocket.setItemCount(1);
                    playerPocket.setPocket(pocket);

                    playerPocketService.save(playerPocket);
                    pocket = pocketService.incrementPocketBelongings(pocket);

                    // add item to result
                    playerPocketList.add(playerPocket);
                }

                return playerPocketList;
            }

            PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(product.getItem0(), product.getCategory(), player.getPocket());
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
                playerPocket.setItemCount(1);
            }
            playerPocket.setPocket(pocket);

            playerPocketService.save(playerPocket);
            if (!existingItem)
                pocketService.incrementPocketBelongings(pocket);

            // add item to result
            playerPocketList.add(playerPocket);
            return playerPocketList;
        }

        Player newPlayer = productService.createNewPlayer(player.getAccount(), product.getForPlayer());
        emit(postActions, new PlayerCreated(newPlayer));

        return playerPocketList;
    }

    private void emit(List<Consumer<? super AddItemHook>> postActions, AddItemHook hook) {
        if (postActions != null && !postActions.isEmpty()) {
            for (Consumer<? super AddItemHook> action : postActions) {
                if (action != null) {
                    action.accept(hook);
                }
            }
        }
    }

    @Override
    @Transactional
    public boolean removeItem(long playerId, int itemIndex, String category, int quantity) {
        Player player = playerService.findWithPocketById(playerId);
        if (player == null) {
            return false;
        }

        PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(itemIndex, category, player.getPocket());
        return removeItemQuantity(playerPocket, quantity);
    }

    @Override
    @Transactional
    public boolean removeItem(long playerId, Long id, int quantity) {
        Player player = playerService.findWithPocketById(playerId);
        if (player == null) {
            return false;
        }

        PlayerPocket playerPocket = playerPocketService.getItemAsPocket(id, player.getPocket());
        return removeItemQuantity(playerPocket, quantity);
    }

    private boolean removeItemQuantity(PlayerPocket playerPocket, int quantity) {
        if (playerPocket == null) {
            return false;
        }

        if (playerPocket.getItemCount() > quantity) {
            playerPocket.setItemCount(playerPocket.getItemCount() - quantity);
            playerPocketService.save(playerPocket);
            return true;
        } else if (playerPocket.getItemCount() == quantity) {
            pocketService.decrementPocketBelongings(playerPocket.getPocket());
            playerPocketService.remove(playerPocket.getId());
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public boolean removeItem(long playerId, Long id) {
        PlayerPocket playerPocket = playerPocketService.findById(id);
        if (playerPocket == null)
            return false;

        Player player = playerService.findById(playerId);
        if (player == null || !player.getPocket().getId().equals(playerPocket.getPocket().getId())) {
            return false;
        }

        pocketService.decrementPocketBelongings(playerPocket.getPocket());
        playerPocketService.remove(playerPocket.getId());
        return true;
    }
}
