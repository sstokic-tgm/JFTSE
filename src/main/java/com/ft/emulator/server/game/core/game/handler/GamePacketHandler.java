package com.ft.emulator.server.game.core.game.handler;

import com.ft.emulator.common.utilities.BitKit;
import com.ft.emulator.common.utilities.StreamUtils;
import com.ft.emulator.common.utilities.StringUtils;
import com.ft.emulator.server.database.model.account.Account;
import com.ft.emulator.server.database.model.challenge.Challenge;
import com.ft.emulator.server.database.model.challenge.ChallengeProgress;
import com.ft.emulator.server.database.model.home.AccountHome;
import com.ft.emulator.server.database.model.home.HomeInventory;
import com.ft.emulator.server.database.model.item.ItemHouse;
import com.ft.emulator.server.database.model.item.ItemHouseDeco;
import com.ft.emulator.server.database.model.item.Product;
import com.ft.emulator.server.database.model.player.ClothEquipment;
import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.database.model.player.QuickSlotEquipment;
import com.ft.emulator.server.database.model.player.StatusPointsAddedDto;
import com.ft.emulator.server.database.model.pocket.PlayerPocket;
import com.ft.emulator.server.database.model.pocket.Pocket;
import com.ft.emulator.server.database.model.tutorial.TutorialProgress;
import com.ft.emulator.server.game.core.item.EItemCategory;
import com.ft.emulator.server.game.core.item.EItemHouseDeco;
import com.ft.emulator.server.game.core.item.EItemUseType;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.game.core.packet.packets.S2CDisconnectAnswerPacket;
import com.ft.emulator.server.game.core.packet.packets.S2CWelcomePacket;
import com.ft.emulator.server.game.core.packet.packets.authserver.S2CLoginAnswerPacket;
import com.ft.emulator.server.game.core.packet.packets.authserver.gameserver.C2SGameServerLoginPacket;
import com.ft.emulator.server.game.core.packet.packets.authserver.gameserver.C2SGameServerRequestPacket;
import com.ft.emulator.server.game.core.packet.packets.authserver.gameserver.S2CGameServerAnswerPacket;
import com.ft.emulator.server.game.core.packet.packets.authserver.gameserver.S2CGameServerLoginPacket;
import com.ft.emulator.server.game.core.packet.packets.battle.C2SQuickSlotUseRequestPacket;
import com.ft.emulator.server.game.core.packet.packets.challenge.*;
import com.ft.emulator.server.game.core.packet.packets.chat.*;
import com.ft.emulator.server.game.core.packet.packets.home.C2SHomeItemsPlaceReqPacket;
import com.ft.emulator.server.game.core.packet.packets.home.S2CHomeDataPacket;
import com.ft.emulator.server.game.core.packet.packets.home.S2CHomeItemsLoadAnswerPacket;
import com.ft.emulator.server.game.core.packet.packets.inventory.*;
import com.ft.emulator.server.game.core.packet.packets.lobby.C2SLobbyUserListRequestPacket;
import com.ft.emulator.server.game.core.packet.packets.lobby.S2CLobbyUserListAnswerPacket;
import com.ft.emulator.server.game.core.packet.packets.lottery.C2SOpenGachaReqPacket;
import com.ft.emulator.server.game.core.packet.packets.lottery.S2COpenGachaAnswerPacket;
import com.ft.emulator.server.game.core.packet.packets.player.C2SPlayerStatusPointChangePacket;
import com.ft.emulator.server.game.core.packet.packets.player.S2CPlayerLevelExpPacket;
import com.ft.emulator.server.game.core.packet.packets.player.S2CPlayerStatusPointChangePacket;
import com.ft.emulator.server.game.core.packet.packets.player.S2CShopMoneyAnswerPacket;
import com.ft.emulator.server.game.core.packet.packets.shop.*;
import com.ft.emulator.server.game.core.packet.packets.tutorial.C2STutorialBeginRequestPacket;
import com.ft.emulator.server.game.core.packet.packets.tutorial.C2STutorialEndPacket;
import com.ft.emulator.server.game.core.packet.packets.tutorial.S2CTutorialProgressAnswerPacket;
import com.ft.emulator.server.game.core.service.*;
import com.ft.emulator.server.game.core.singleplay.challenge.ChallengeBasicGame;
import com.ft.emulator.server.game.core.singleplay.challenge.ChallengeBattleGame;
import com.ft.emulator.server.game.core.singleplay.challenge.GameMode;
import com.ft.emulator.server.game.core.singleplay.tutorial.TutorialGame;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import com.ft.emulator.server.shared.module.GameHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GamePacketHandler {
    private final GameHandler gameHandler;

    private final AuthenticationService authenticationService;
    private final PlayerService playerService;
    private final ClothEquipmentService clothEquipmentService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;
    private final PocketService pocketService;
    private final HomeService homeService;
    private final PlayerPocketService playerPocketService;
    private final ChallengeService challengeService;
    private final TutorialService tutorialService;
    private final ProductService productService;
    private final LotteryService lotteryService;

    public GameHandler getGameHandler() {
        return gameHandler;
    }

    public void sendWelcomePacket(Connection connection) {
        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(0, 0, 0, 0);
        connection.sendTCP(welcomePacket);
    }

    public void handleGameServerLoginPacket(Connection connection, Packet packet) {
        C2SGameServerLoginPacket gameServerLoginPacket = new C2SGameServerLoginPacket(packet);

        Player player = playerService.findByIdFetched((long) gameServerLoginPacket.getPlayerId());
        if (player != null && player.getAccount() != null) {
            Client client = connection.getClient();
            Account account = player.getAccount();

            // set last login date
            account.setLastLogin(new Date());
            // mark as logged in
            account.setStatus((int) S2CLoginAnswerPacket.ACCOUNT_ALREADY_LOGGED_IN);
            account = authenticationService.updateAccount(account);

            client.setAccount(account);
            client.setActivePlayer(player);
            connection.setClient(client);

            S2CGameServerLoginPacket gameServerLoginAnswerPacket = new S2CGameServerLoginPacket((char) 0, (byte) 1);
            connection.sendTCP(gameServerLoginAnswerPacket);
        }
        else {
            S2CGameServerLoginPacket gameServerLoginAnswerPacket = new S2CGameServerLoginPacket((char) -1, (byte) 0);
            connection.sendTCP(gameServerLoginAnswerPacket);
        }
    }

    public void handleGameServerDataRequestPacket(Connection connection, Packet packet) {
        Client client = connection.getClient();
        Player player = client.getActivePlayer();
        Account account = client.getAccount();

        C2SGameServerRequestPacket gameServerRequestPacket = new C2SGameServerRequestPacket(packet);
        byte requestType = gameServerRequestPacket.getRequestType();

        // init data request packets and pass level & exp and home/house data
        if (requestType == 0) {
            S2CGameServerAnswerPacket gameServerAnswerPacket = new S2CGameServerAnswerPacket(requestType, (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);

            // init data request packets
            gameServerAnswerPacket = new S2CGameServerAnswerPacket((byte) 1, (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);

            S2CPlayerLevelExpPacket playerLevelExpPacket = new S2CPlayerLevelExpPacket(player.getLevel(), player.getExpPoints());
            connection.sendTCP(playerLevelExpPacket);

            AccountHome accountHome = homeService.findAccountHomeByAccountId(account.getId());

            S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
            connection.sendTCP(homeDataPacket);
        }
        else if (requestType == 1) {
            S2CGameServerAnswerPacket gameServerAnswerPacket = new S2CGameServerAnswerPacket(requestType, (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);

            gameServerAnswerPacket = new S2CGameServerAnswerPacket((byte) 2, (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);
        }
        // pass inventory & equipped items
        else if (requestType == 2) {
            S2CGameServerAnswerPacket gameServerAnswerPacket = new S2CGameServerAnswerPacket(requestType, (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);

            List<PlayerPocket> playerPocketList = playerPocketService.getPlayerPocketItems(player.getPocket());
            StreamUtils.batches(playerPocketList, 10).forEach(pocketList -> {
                    S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(pocketList);
                    connection.sendTCP(inventoryDataPacket);
                });

            StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
            Map<String, Integer> equippedCloths = clothEquipmentService.getEquippedCloths(player);
            List<Integer> equippedQuickSlots = quickSlotEquipmentService.getEquippedQuickSlots(player);

            S2CPlayerStatusPointChangePacket playerStatusPointChangePacket = new S2CPlayerStatusPointChangePacket(player, statusPointsAddedDto);
            connection.sendTCP(playerStatusPointChangePacket);

            S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, equippedCloths, player, statusPointsAddedDto);
            connection.sendTCP(inventoryWearClothAnswerPacket);

            S2CInventoryWearQuickAnswerPacket inventoryWearQuickAnswerPacket = new S2CInventoryWearQuickAnswerPacket(equippedQuickSlots);
            connection.sendTCP(inventoryWearQuickAnswerPacket);

            gameServerAnswerPacket = new S2CGameServerAnswerPacket((byte) (requestType + 1), (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);
        }
        else {
            S2CGameServerAnswerPacket gameServerAnswerPacket = new S2CGameServerAnswerPacket(requestType, (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);
        }
    }

    public void handleHomeItemsLoadRequestPacket(Connection connection, Packet packet) {
        AccountHome accountHome = homeService.findAccountHomeByAccountId(connection.getClient().getAccount().getId());
        List<HomeInventory> homeInventoryList = homeService.findAllByAccountHome(accountHome);

        S2CHomeItemsLoadAnswerPacket homeItemsLoadAnswerPacket = new S2CHomeItemsLoadAnswerPacket(homeInventoryList);
        connection.sendTCP(homeItemsLoadAnswerPacket);
    }

    public void handleHomeItemsPlaceRequestPacket(Connection connection, Packet packet) {
        C2SHomeItemsPlaceReqPacket homeItemsPlaceReqPacket = new C2SHomeItemsPlaceReqPacket(packet);
        List<Map<String, Object>> homeItemDataList = homeItemsPlaceReqPacket.getHomeItemDataList();

        AccountHome accountHome = homeService.findAccountHomeByAccountId(connection.getClient().getAccount().getId());

        homeItemDataList.forEach(hidl -> {
                int inventoryItemId = (int)hidl.get("inventoryItemId");

                PlayerPocket playerPocket = playerPocketService.getItemAsPocket((long) inventoryItemId, connection.getClient().getActivePlayer().getPocket());
                if (playerPocket != null) {
                    int itemCount = playerPocket.getItemCount();

                    // those items are deco items -> its placed on the wall
                    if (itemCount % 3 != 0)
                        --itemCount;
                    else
                        itemCount = 0;

                    if (itemCount == 0) {
                        playerPocketService.remove((long) inventoryItemId);
                        pocketService.decrementPocketBelongings(connection.getClient().getActivePlayer().getPocket());

                        S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(inventoryItemId);
                        connection.sendTCP(inventoryItemRemoveAnswerPacket);
                    }
                    else {
                        playerPocket.setItemCount(itemCount);
                        playerPocketService.save(playerPocket);
                    }

                    int itemIndex = (int) hidl.get("itemIndex");
                    byte unk0 = (byte) hidl.get("unk4");
                    byte unk1 = (byte) hidl.get("unk5");
                    byte xPos = (byte) hidl.get("xPos");
                    byte yPos = (byte) hidl.get("yPos");

                    HomeInventory homeInventory = new HomeInventory();
                    homeInventory.setId((long) inventoryItemId);
                    homeInventory.setAccountHome(accountHome);
                    homeInventory.setItemIndex(itemIndex);
                    homeInventory.setUnk0(unk0);
                    homeInventory.setUnk1(unk1);
                    homeInventory.setXPos(xPos);
                    homeInventory.setYPos(yPos);

                    homeInventory = homeService.save(homeInventory);

                    homeService.updateAccountHomeStatsByHomeInventory(accountHome, homeInventory, true);
                }
            });

        S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
        connection.sendTCP(homeDataPacket);
    }

    public void handleHomeItemClearRequestPacket(Connection connection, Packet packet) {

        AccountHome accountHome = homeService.findAccountHomeByAccountId(connection.getClient().getAccount().getId());
        List<HomeInventory> homeInventoryList = homeService.findAllByAccountHome(accountHome);

        homeInventoryList.forEach(hil -> {
                PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndex(hil.getItemIndex(), connection.getClient().getActivePlayer().getPocket());
                ItemHouseDeco itemHouseDeco = homeService.findItemHouseDecoByItemIndex(hil.getItemIndex());

                // create a new one if null, null indicates that all items are placed
                if (playerPocket == null) {
                    playerPocket = new PlayerPocket();
                    playerPocket.setItemIndex(hil.getItemIndex());
                    playerPocket.setPocket(connection.getClient().getActivePlayer().getPocket());
                    playerPocket.setItemCount(itemHouseDeco.getKind().equals(EItemHouseDeco.DECO.getName()) ? 3 : 1);
                    playerPocket.setCategory(EItemCategory.HOUSE_DECO.getName());
                    playerPocket.setUseType(StringUtils.firstCharToUpperCase(EItemUseType.COUNT.getName().toLowerCase()));

                    pocketService.incrementPocketBelongings(connection.getClient().getActivePlayer().getPocket());
                }
                else {
                    playerPocket.setItemCount(playerPocket.getItemCount() + (itemHouseDeco.getKind().equals(EItemHouseDeco.DECO.getName()) ? 3 : 1));
                }

                playerPocketService.save(playerPocket);

                homeService.updateAccountHomeStatsByHomeInventory(accountHome, hil, false);
                homeService.removeItemFromHomeInventory(hil.getId());
            });

        S2CHomeItemsLoadAnswerPacket homeItemsLoadAnswerPacket = new S2CHomeItemsLoadAnswerPacket(new ArrayList<>());
        connection.sendTCP(homeItemsLoadAnswerPacket);

        S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
        connection.sendTCP(homeDataPacket);

        List<PlayerPocket> playerPocketList = playerPocketService.getPlayerPocketItems(connection.getClient().getActivePlayer().getPocket());
        StreamUtils.batches(playerPocketList, 10)
            .forEach(pocketList -> {
                    S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(pocketList);
                    connection.sendTCP(inventoryDataPacket);
                });
    }

    public void handleInventoryItemSellPackets(Connection connection, Packet packet) {
        switch (packet.getPacketId()) {
        case PacketID.C2SInventorySellReq: {
            byte status = S2CInventorySellAnswerPacket.SUCCESS;

            C2SInventorySellReqPacket inventorySellReqPacket = new C2SInventorySellReqPacket(packet);
            int itemPocketId = inventorySellReqPacket.getItemPocketId();

            PlayerPocket playerPocket = playerPocketService.getItemAsPocket((long) itemPocketId, connection.getClient().getActivePlayer().getPocket());

            if(playerPocket == null) {
                status = S2CInventorySellAnswerPacket.NO_ITEM;

                S2CInventorySellAnswerPacket inventorySellAnswerPacket = new S2CInventorySellAnswerPacket(status, 0, 0);
                connection.sendTCP(inventorySellAnswerPacket);
                break;
            }

            int sellPrice = playerPocketService.getSellPrice(playerPocket);

            S2CInventorySellAnswerPacket inventorySellAnswerPacket = new S2CInventorySellAnswerPacket(status, itemPocketId, sellPrice);
            connection.sendTCP(inventorySellAnswerPacket);
        } break;
        case PacketID.C2SInventorySellItemCheckReq: {
            byte status = S2CInventorySellItemCheckAnswerPacket.SUCCESS;

            C2SInventorySellItemCheckReqPacket inventorySellItemCheckReqPacket = new C2SInventorySellItemCheckReqPacket(packet);
            int itemPocketId = inventorySellItemCheckReqPacket.getItemPocketId();

            Pocket pocket = connection.getClient().getActivePlayer().getPocket();
            PlayerPocket playerPocket = playerPocketService.getItemAsPocket((long) itemPocketId, pocket);

            if(playerPocket == null) {
                status = S2CInventorySellAnswerPacket.NO_ITEM;

                S2CInventorySellItemCheckAnswerPacket inventorySellItemCheckAnswerPacket = new S2CInventorySellItemCheckAnswerPacket(status);
                connection.sendTCP(inventorySellItemCheckAnswerPacket);
                break;
            }

            int sellPrice = playerPocketService.getSellPrice(playerPocket);

            S2CInventorySellItemCheckAnswerPacket inventorySellItemCheckAnswerPacket = new S2CInventorySellItemCheckAnswerPacket(status);
            connection.sendTCP(inventorySellItemCheckAnswerPacket);

            S2CInventorySellItemAnswerPacket inventorySellItemAnswerPacket = new S2CInventorySellItemAnswerPacket((char) playerPocket.getItemCount().intValue(), itemPocketId);
            connection.sendTCP(inventorySellItemAnswerPacket);

            playerPocketService.remove(playerPocket.getId());
            pocket = pocketService.decrementPocketBelongings(connection.getClient().getActivePlayer().getPocket());
            connection.getClient().getActivePlayer().setPocket(pocket);

            Player player = connection.getClient().getActivePlayer();
            player = playerService.updateMoney(player, sellPrice);

            S2CShopMoneyAnswerPacket shopMoneyAnswerPacket = new S2CShopMoneyAnswerPacket(player);
            connection.sendTCP(shopMoneyAnswerPacket);

            connection.getClient().setActivePlayer(player);
        } break;
        }
    }

    public void handleInventoryWearClothPacket(Connection connection, Packet packet) {
        C2SInventoryWearClothReqPacket inventoryWearClothReqPacket = new C2SInventoryWearClothReqPacket(packet);

        Player player = connection.getClient().getActivePlayer();
        ClothEquipment clothEquipment = player.getClothEquipment();

        clothEquipmentService.updateCloths(clothEquipment, inventoryWearClothReqPacket);
        player.setClothEquipment(clothEquipment);

        player = playerService.save(player);
        connection.getClient().setActivePlayer(player);

        StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);

        S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, inventoryWearClothReqPacket, player, statusPointsAddedDto);
        connection.sendTCP(inventoryWearClothAnswerPacket);
    }

    public void handleInventoryWearQuickPacket(Connection connection, Packet packet) {
        C2SInventoryWearQuickReqPacket inventoryWearQuickReqPacket = new C2SInventoryWearQuickReqPacket(packet);

        Player player = connection.getClient().getActivePlayer();
        QuickSlotEquipment quickSlotEquipment = player.getQuickSlotEquipment();

        quickSlotEquipmentService.updateQuickSlots(quickSlotEquipment, inventoryWearQuickReqPacket.getQuickSlotList());
        player.setQuickSlotEquipment(quickSlotEquipment);

        player = playerService.save(player);
        connection.getClient().setActivePlayer(player);

        S2CInventoryWearQuickAnswerPacket inventoryWearQuickAnswerPacket = new S2CInventoryWearQuickAnswerPacket(inventoryWearQuickReqPacket.getQuickSlotList());
        connection.sendTCP(inventoryWearQuickAnswerPacket);
    }

    public void handleInventoryItemTimeExpiredPacket(Connection connection, Packet packet) {
        C2SInventoryItemTimeExpiredReqPacket inventoryItemTimeExpiredReqPacket = new C2SInventoryItemTimeExpiredReqPacket(packet);

        Pocket pocket = connection.getClient().getActivePlayer().getPocket();

        playerPocketService.remove((long) inventoryItemTimeExpiredReqPacket.getItemPocketId());
        pocket = pocketService.decrementPocketBelongings(pocket);

        connection.getClient().getActivePlayer().setPocket(pocket);

        S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(inventoryItemTimeExpiredReqPacket.getItemPocketId());
        connection.sendTCP(inventoryItemRemoveAnswerPacket);
    }

    public void handleShopMoneyRequestPacket(Connection connection, Packet packet) {
        Player player = playerService.findByIdFetched(connection.getClient().getActivePlayer().getId());
        connection.getClient().setActivePlayer(player);

        S2CShopMoneyAnswerPacket shopMoneyAnswerPacket = new S2CShopMoneyAnswerPacket(player);
        connection.sendTCP(shopMoneyAnswerPacket);
    }

    public void handleShopBuyRequestPacket(Connection connection, Packet packet) {
        C2SShopBuyPacket shopBuyPacket = new C2SShopBuyPacket(packet);

        Map<Integer, Byte> itemList = shopBuyPacket.getItemList();

        Map<Product, Byte> productList = productService.findProductsByItemList(itemList);

        Player player = connection.getClient().getActivePlayer();

        int gold = player.getGold();
        int costs = productList.keySet()
            .stream()
            .mapToInt(Product::getPrice0)
            .sum();

        int result = gold - costs;

        List<PlayerPocket> playerPocketList = new ArrayList<>();

        if (result >= 0) {
            for (Map.Entry<Product, Byte> data : productList.entrySet()) {
                Product product = data.getKey();
                byte option = data.getValue();

                // prevent user from buying pet till it'simplemented
                if (product.getCategory().equals(EItemCategory.PET_CHAR.getName())) {
                    result += product.getPrice0();
                    continue;
                }

                if (!product.getCategory().equals(EItemCategory.CHAR.getName())) {
                    if (product.getCategory().equals(EItemCategory.HOUSE.getName())) {

                        ItemHouse itemHouse = homeService.findItemHouseByItemIndex(product.getItem0());
                        AccountHome accountHome = homeService.findAccountHomeByAccountId(connection.getClient().getAccount().getId());

                        accountHome.setLevel(itemHouse.getLevel());
                        accountHome = homeService.save(accountHome);

                        S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
                        connection.sendTCP(homeDataPacket);
                    } else {
                        // gold back
                        if (product.getGoldBack() != 0)
                            result += product.getGoldBack();

                        Pocket pocket = connection.getClient().getActivePlayer().getPocket();

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

                            // case if set has player included, items are transferred to the new player
                            if (product.getForPlayer() != -1) {

                                Player newPlayer = productService.createNewPlayer(connection.getClient().getAccount(), product.getForPlayer());
                                Pocket newPlayerPocket = pocketService.findById(newPlayer.getPocket().getId());

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
                            }
                            else {
                                for (Integer itemIndex : itemPartList) {

                                    PlayerPocket playerPocket = new PlayerPocket();
                                    playerPocket.setCategory(product.getCategory());
                                    playerPocket.setItemIndex(itemIndex);
                                    playerPocket.setUseType(product.getUseType());
                                    playerPocket.setItemCount(1);
                                    playerPocket.setPocket(pocket);

                                    playerPocket = playerPocketService.save(playerPocket);
                                    pocket = pocketService.incrementPocketBelongings(pocket);

                                    // add item to result
                                    playerPocketList.add(playerPocket);
                                }
                            }
                        } else {
                            PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndex(product.getItem0(), player.getPocket());
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

                            if (option == 0)
                                playerPocket.setItemCount(product.getUse0() == 0 ? 1 : product.getUse0());
                            else if (option == 1)
                                playerPocket.setItemCount(product.getUse1());
                            else if (option == 2)
                                playerPocket.setItemCount(product.getUse2());

                            playerPocket.setItemCount(playerPocket.getItemCount() + existingItemCount);

                            if (playerPocket.getUseType().equalsIgnoreCase(EItemUseType.TIME.getName())) {
                                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                cal.add(Calendar.DAY_OF_MONTH, playerPocket.getItemCount());

                                playerPocket.setCreated(cal.getTime());
                            }
                            playerPocket.setPocket(pocket);

                            playerPocket = playerPocketService.save(playerPocket);
                            if (!existingItem)
                                pocket = pocketService.incrementPocketBelongings(pocket);

                            // add item to result
                            playerPocketList.add(playerPocket);
                        }

                        connection.getClient().getActivePlayer().setPocket(pocket);
                    }
                }
                else {
                    productService.createNewPlayer(connection.getClient().getAccount(), product.getForPlayer());
                }
            }

            S2CShopBuyPacket shopBuyPacketAnswer = new S2CShopBuyPacket(S2CShopBuyPacket.SUCCESS, playerPocketList);
            connection.sendTCP(shopBuyPacketAnswer);

            player = playerService.setMoney(player, result);

            S2CShopMoneyAnswerPacket shopMoneyAnswerPacket = new S2CShopMoneyAnswerPacket(player);
            connection.sendTCP(shopMoneyAnswerPacket);

            connection.getClient().setActivePlayer(player);
        } else {
            S2CShopBuyPacket shopBuyPacketAnswer = new S2CShopBuyPacket(S2CShopBuyPacket.NEED_MORE_GOLD, null);
            connection.sendTCP(shopBuyPacketAnswer);
        }
    }

    public void handleShopRequestDataPackets(Connection connection, Packet packet) {
        switch (packet.getPacketId()) {
        case PacketID.C2SShopRequestDataPrepare: {
            C2SShopRequestDataPreparePacket shopRequestDataPreparePacket = new C2SShopRequestDataPreparePacket(packet);
            byte category = shopRequestDataPreparePacket.getCategory();
            byte part = shopRequestDataPreparePacket.getPart();
            byte player = shopRequestDataPreparePacket.getPlayer();

            int productListSize = productService.getProductListSize(category, part, player);

            S2CShopAnswerDataPreparePacket shopAnswerDataPreparePacket = new S2CShopAnswerDataPreparePacket(category, part, player, productListSize);
            connection.sendTCP(shopAnswerDataPreparePacket);
        } break;
        case PacketID.C2SShopRequestData: {
            C2SShopRequestDataPacket shopRequestDataPacket = new C2SShopRequestDataPacket(packet);

            byte category = shopRequestDataPacket.getCategory();
            byte part = shopRequestDataPacket.getPart();
            byte player = shopRequestDataPacket.getPlayer();
            int page = BitKit.fromUnsignedInt(shopRequestDataPacket.getPage());

            List<Product> productList = productService.getProductList(category, part, player, page);

            S2CShopAnswerDataPacket shopAnswerDataPacket = new S2CShopAnswerDataPacket(productList.size(), productList);
            connection.sendTCP(shopAnswerDataPacket);
        } break;
        }
    }

    public void handlePlayerStatusPointChangePacket(Connection connection, Packet packet) {
        C2SPlayerStatusPointChangePacket playerStatusPointChangePacket = new C2SPlayerStatusPointChangePacket(packet);

        Player player = connection.getClient().getActivePlayer();

        // we can't change; attributes should be server sided
        if (player.getStatusPoints() == 0) {
            S2CPlayerStatusPointChangePacket playerStatusPointChangeAnswerPacket = new S2CPlayerStatusPointChangePacket(player, new StatusPointsAddedDto());
            connection.sendTCP(playerStatusPointChangeAnswerPacket);
        }
        else if (player.getStatusPoints() > 0 && playerStatusPointChangePacket.getStatusPoints() >= 0) {
            if (playerService.isStatusPointHack(playerStatusPointChangePacket, player)) {
                S2CPlayerStatusPointChangePacket playerStatusPointChangeAnswerPacket = new S2CPlayerStatusPointChangePacket(player, new StatusPointsAddedDto());
                connection.sendTCP(playerStatusPointChangeAnswerPacket);
            } else {
                player.setStrength(playerStatusPointChangePacket.getStrength());
                player.setStamina(playerStatusPointChangePacket.getStamina());
                player.setDexterity(playerStatusPointChangePacket.getDexterity());
                player.setWillpower(playerStatusPointChangePacket.getWillpower());
                player.setStatusPoints(playerStatusPointChangePacket.getStatusPoints());

                player = playerService.save(player);

                connection.getClient().setActivePlayer(player);

                StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);

                S2CPlayerStatusPointChangePacket playerStatusPointChangeAnswerPacket = new S2CPlayerStatusPointChangePacket(player, statusPointsAddedDto);
                connection.sendTCP(playerStatusPointChangeAnswerPacket);
            }
        }
    }

    public void handleChallengeProgressRequestPacket(Connection connection, Packet packet) {
        List<ChallengeProgress> challengeProgressList = challengeService.findAllByPlayerIdFetched(connection.getClient().getActivePlayer().getId());

        S2CChallengeProgressAnswerPacket challengeProgressAnswerPacket = new S2CChallengeProgressAnswerPacket(challengeProgressList);
        connection.sendTCP(challengeProgressAnswerPacket);
    }

    public void handleTutorialProgressRequestPacket(Connection connection, Packet packet) {
        List<TutorialProgress> tutorialProgressList = tutorialService.findAllByPlayerIdFetched(connection.getClient().getActivePlayer().getId());

        S2CTutorialProgressAnswerPacket tutorialProgressAnswerPacket = new S2CTutorialProgressAnswerPacket(tutorialProgressList);
        connection.sendTCP(tutorialProgressAnswerPacket);
    }

    public void handleChallengeBeginRequestPacket(Connection connection, Packet packet) {
        C2SChallengeBeginRequestPacket challengeBeginRequestPacket = new C2SChallengeBeginRequestPacket(packet);
        int challengeId = challengeBeginRequestPacket.getChallengeId();

        Challenge currentChallenge = challengeService.findChallengeByChallengeIndex(challengeId);

        if (currentChallenge.getGameMode() == GameMode.BASIC)
            connection.getClient().setActiveChallengeGame(new ChallengeBasicGame(challengeId));
        else if (currentChallenge.getGameMode() == GameMode.BATTLE)
            connection.getClient().setActiveChallengeGame(new ChallengeBattleGame(challengeId));

        Packet answer = new Packet(PacketID.C2STutorialBegin);
        answer.write((char) 1);
        connection.sendTCP(answer);
    }

    public void handleChallengeHpPacket(Connection connection, Packet packet) {
        C2SChallengeHpPacket challengeHpPacket = new C2SChallengeHpPacket(packet);

        if (connection.getClient().getActiveChallengeGame() instanceof ChallengeBattleGame) {

            ((ChallengeBattleGame) connection.getClient().getActiveChallengeGame()).setMaxPlayerHp(challengeHpPacket.getPlayerHp());
            ((ChallengeBattleGame) connection.getClient().getActiveChallengeGame()).setMaxNpcHp(challengeHpPacket.getNpcHp());
        }
    }

    public void handleChallengePointPacket(Connection connection, Packet packet) {
        C2SChallengePointPacket challengePointPacket = new C2SChallengePointPacket(packet);

        if (connection.getClient().getActiveChallengeGame() != null) {
            ((ChallengeBasicGame) connection.getClient().getActiveChallengeGame()).setPoints(challengePointPacket.getPointsPlayer(), challengePointPacket.getPointsNpc());

            if (connection.getClient().getActiveChallengeGame().isFinished()) {
                boolean win = ((ChallengeBasicGame) connection.getClient().getActiveChallengeGame()).getSetsPlayer() == 2;
                challengeService.finishGame(connection, win);

                connection.getClient().setActiveChallengeGame(null);
            }
        }
    }

    public void handleChallengeDamagePacket(Connection connection, Packet packet) {
        C2SChallengeDamagePacket challengeDamagePacket = new C2SChallengeDamagePacket(packet);

        if (connection.getClient().getActiveChallengeGame() != null) {
            ((ChallengeBattleGame) connection.getClient().getActiveChallengeGame()).setHp(challengeDamagePacket.getPlayer(), challengeDamagePacket.getDmg());

            if (connection.getClient().getActiveChallengeGame().isFinished()) {
                boolean win = ((ChallengeBattleGame) connection.getClient().getActiveChallengeGame()).getPlayerHp() > 0;
                challengeService.finishGame(connection, win);

                connection.getClient().setActiveChallengeGame(null);
            }
        }
    }

    public void handleQuickSlotUseRequest(Connection connection, Packet packet) {
        C2SQuickSlotUseRequestPacket quickSlotUseRequestPacket = new C2SQuickSlotUseRequestPacket(packet);

        Player player = connection.getClient().getActivePlayer();
        Pocket pocket = player.getPocket();

        PlayerPocket playerPocket = playerPocketService.getItemAsPocket((long) quickSlotUseRequestPacket.getQuickSlotId(), pocket);
        int itemCount = playerPocket.getItemCount() - 1;

        if (itemCount <= 0) {

            playerPocketService.remove(playerPocket.getId());
            pocket = pocketService.decrementPocketBelongings(pocket);
            connection.getClient().getActivePlayer().setPocket(pocket);

            QuickSlotEquipment quickSlotEquipment = player.getQuickSlotEquipment();
            quickSlotEquipmentService.updateQuickSlots(quickSlotEquipment, quickSlotUseRequestPacket.getQuickSlotId());
            player.setQuickSlotEquipment(quickSlotEquipment);

            player = playerService.save(player);
            connection.getClient().setActivePlayer(player);

            S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(quickSlotUseRequestPacket.getQuickSlotId());
            connection.sendTCP(inventoryItemRemoveAnswerPacket);
        } else {
            playerPocket.setItemCount(itemCount);
            playerPocketService.save(playerPocket);
        }
    }

    public void handleChallengeSetPacket(Connection connection, Packet packet) {
        // empty..
    }

    public void handleTutorialBeginPacket(Connection connection, Packet packet) {
        C2STutorialBeginRequestPacket tutorialBeginRequestPacket = new C2STutorialBeginRequestPacket(packet);
        int tutorialId = tutorialBeginRequestPacket.getTutorialId();

        connection.getClient().setActiveTutorialGame(new TutorialGame(tutorialId));

        Packet answer = new Packet(PacketID.C2STutorialBegin);
        answer.write((char) 1);
        connection.sendTCP(answer);
    }

    public void handleTutorialEndPacket(Connection connection, Packet packet) {
        C2STutorialEndPacket tutorialEndPacket = new C2STutorialEndPacket(packet);
        connection.getClient().getActiveTutorialGame().finishTutorial();

        tutorialService.finishGame(connection);

        connection.getClient().setActiveTutorialGame(null);
    }

    public void handleLobbyUserListReqPacket(Connection connection, Packet packet) {
        C2SLobbyUserListRequestPacket lobbyUserListRequestPacket = new C2SLobbyUserListRequestPacket(packet);
        byte page = lobbyUserListRequestPacket.getPage();

        List<Player> lobbyPlayerList = this.gameHandler.getPlayersInLobby().stream()
            .skip(page == 1 ? 0 : (page * 10) - 10)
            .limit(10)
            .collect(Collectors.toList());

        S2CLobbyUserListAnswerPacket lobbyUserListAnswerPacket = new S2CLobbyUserListAnswerPacket(lobbyPlayerList);
        connection.sendTCP(lobbyUserListAnswerPacket);
    }

    public void handleChatMessagePackets(Connection connection, Packet packet) {
        switch (packet.getPacketId()) {
        case PacketID.C2SChatLobbyReq: {
            C2SChatLobbyReqPacket chatLobbyReqPacket = new C2SChatLobbyReqPacket(packet);
            S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket(chatLobbyReqPacket.getUnk(), connection.getClient().getActivePlayer().getName(), chatLobbyReqPacket.getMessage());

            this.getGameHandler().getClientList().forEach(cl -> cl.getConnection().sendTCP(chatLobbyAnswerPacket));
        } break;
        case PacketID.C2SChatRoomReq: {
            C2SChatRoomReqPacket chatRoomReqPacket = new C2SChatRoomReqPacket(packet);
            S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket(chatRoomReqPacket.getType(), connection.getClient().getActivePlayer().getName(), chatRoomReqPacket.getMessage());
        } break;
        case PacketID.C2SWhisperReq: {
            C2SWhisperReqPacket whisperReqPacket = new C2SWhisperReqPacket(packet);
            S2CWhisperAnswerPacket whisperAnswerPacket = new S2CWhisperAnswerPacket(connection.getClient().getActivePlayer().getName(), whisperReqPacket.getReceiverName(), whisperReqPacket.getMessage());

            this.getGameHandler().getClientList().stream()
                .filter(cl -> cl.getActivePlayer().getName().equals(whisperReqPacket.getReceiverName()))
                .findAny()
                .ifPresent(cl -> cl.getConnection().sendTCP(whisperAnswerPacket));

            connection.sendTCP(whisperAnswerPacket);
        } break;
        }
    }

    public void handleLobbyJoinLeave(Connection connection, boolean joined) {
        connection.getClient().setInLobby(joined);
    }

    public void handleEmblemListRequestPacket(Connection connection, Packet packet) {
        // empty..
    }

    public void handleOpenGachaRequestPacket(Connection connection, Packet packet) {
        C2SOpenGachaReqPacket openGachaReqPacket = new C2SOpenGachaReqPacket(packet);
        long playerPocketId = openGachaReqPacket.getPlayerPocketId();
        int productIndex = openGachaReqPacket.getProductIndex();

        List<PlayerPocket> playerPocketList = lotteryService.drawLottery(connection, playerPocketId, productIndex);

        S2COpenGachaAnswerPacket openGachaAnswerPacket = new S2COpenGachaAnswerPacket(playerPocketList);
        connection.sendTCP(openGachaAnswerPacket);
    }

    public void handleDisconnectPacket(Connection connection, Packet packet) {

        // reset pocket
        List<PlayerPocket> playerPocketList = playerPocketService.getPlayerPocketItems(connection.getClient().getActivePlayer().getPocket());
        playerPocketList.forEach(pp -> {
                S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket((int) pp.getId().longValue());
                connection.sendTCP(inventoryItemRemoveAnswerPacket);
            });

        // reset status
        Account account = authenticationService.findAccountById(connection.getClient().getAccount().getId());
        account.setStatus((int) S2CLoginAnswerPacket.SUCCESS);
        authenticationService.updateAccount(account);

        S2CDisconnectAnswerPacket disconnectAnswerPacket = new S2CDisconnectAnswerPacket();
        connection.sendTCP(disconnectAnswerPacket);
    }

    public void handleDisconnected(Connection connection) {
        if (connection.getClient().getAccount() != null) {
            // reset status
            Account account = authenticationService.findAccountById(connection.getClient().getAccount().getId());
            account.setStatus((int) S2CLoginAnswerPacket.SUCCESS);
            authenticationService.updateAccount(account);
        }

        gameHandler.removeClient(connection.getClient());

        connection.setClient(null);
        connection.close();
    }

    public void handleUnknown(Connection connection, Packet packet) {
        Packet unknownAnswer = new Packet((char) (packet.getPacketId() + 1));
        if (unknownAnswer.getPacketId() == (char) 0x200E) {
            unknownAnswer.write((char) 1);
        } else {
            unknownAnswer.write((short) 0);
        }
        connection.sendTCP(unknownAnswer);
    }
}
