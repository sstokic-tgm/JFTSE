package com.jftse.emulator.server.game.core.game.handler;

import com.jftse.emulator.common.GlobalSettings;
import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.emulator.common.utilities.StreamUtils;
import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.database.model.anticheat.ClientWhitelist;
import com.jftse.emulator.server.database.model.challenge.Challenge;
import com.jftse.emulator.server.database.model.challenge.ChallengeProgress;
import com.jftse.emulator.server.database.model.gameserver.GameServer;
import com.jftse.emulator.server.database.model.guild.Guild;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.home.AccountHome;
import com.jftse.emulator.server.database.model.home.HomeInventory;
import com.jftse.emulator.server.database.model.item.ItemChar;
import com.jftse.emulator.server.database.model.item.ItemHouse;
import com.jftse.emulator.server.database.model.item.ItemHouseDeco;
import com.jftse.emulator.server.database.model.item.Product;
import com.jftse.emulator.server.database.model.messaging.*;
import com.jftse.emulator.server.database.model.player.*;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.database.model.pocket.Pocket;
import com.jftse.emulator.server.database.model.tutorial.TutorialProgress;
import com.jftse.emulator.server.game.core.constants.GameMode;
import com.jftse.emulator.server.game.core.constants.RoomPositionState;
import com.jftse.emulator.server.game.core.constants.RoomStatus;
import com.jftse.emulator.server.game.core.game.handler.matchplay.BasicModeHandler;
import com.jftse.emulator.server.game.core.game.handler.matchplay.BattleModeHandler;
import com.jftse.emulator.server.game.core.game.handler.matchplay.GuardianModeHandler;
import com.jftse.emulator.server.game.core.item.EItemCategory;
import com.jftse.emulator.server.game.core.item.EItemHouseDeco;
import com.jftse.emulator.server.game.core.item.EItemUseType;
import com.jftse.emulator.server.game.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.game.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.game.core.matchplay.basic.MatchplayBasicGame;
import com.jftse.emulator.server.game.core.matchplay.basic.MatchplayBattleGame;
import com.jftse.emulator.server.game.core.matchplay.basic.MatchplayGuardianGame;
import com.jftse.emulator.server.game.core.matchplay.event.PacketEventHandler;
import com.jftse.emulator.server.game.core.matchplay.event.RunnableEventHandler;
import com.jftse.emulator.server.game.core.matchplay.room.GameSession;
import com.jftse.emulator.server.game.core.matchplay.room.Room;
import com.jftse.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.game.core.packet.packets.S2CDisconnectAnswerPacket;
import com.jftse.emulator.server.game.core.packet.packets.S2CWelcomePacket;
import com.jftse.emulator.server.game.core.packet.packets.authserver.S2CLoginAnswerPacket;
import com.jftse.emulator.server.game.core.packet.packets.authserver.gameserver.C2SGameServerLoginPacket;
import com.jftse.emulator.server.game.core.packet.packets.authserver.gameserver.C2SGameServerRequestPacket;
import com.jftse.emulator.server.game.core.packet.packets.authserver.gameserver.S2CGameServerAnswerPacket;
import com.jftse.emulator.server.game.core.packet.packets.authserver.gameserver.S2CGameServerLoginPacket;
import com.jftse.emulator.server.game.core.packet.packets.battle.C2SQuickSlotUseRequestPacket;
import com.jftse.emulator.server.game.core.packet.packets.challenge.*;
import com.jftse.emulator.server.game.core.packet.packets.chat.*;
import com.jftse.emulator.server.game.core.packet.packets.guild.*;
import com.jftse.emulator.server.game.core.packet.packets.home.C2SHomeItemsPlaceReqPacket;
import com.jftse.emulator.server.game.core.packet.packets.home.S2CHomeDataPacket;
import com.jftse.emulator.server.game.core.packet.packets.home.S2CHomeItemsLoadAnswerPacket;
import com.jftse.emulator.server.game.core.packet.packets.inventory.*;
import com.jftse.emulator.server.game.core.packet.packets.lobby.*;
import com.jftse.emulator.server.game.core.packet.packets.lobby.room.*;
import com.jftse.emulator.server.game.core.packet.packets.lottery.C2SOpenGachaReqPacket;
import com.jftse.emulator.server.game.core.packet.packets.lottery.S2COpenGachaAnswerPacket;
import com.jftse.emulator.server.game.core.packet.packets.matchplay.*;
import com.jftse.emulator.server.game.core.packet.packets.messaging.*;
import com.jftse.emulator.server.game.core.packet.packets.player.*;
import com.jftse.emulator.server.game.core.packet.packets.ranking.C2SRankingDataRequestPacket;
import com.jftse.emulator.server.game.core.packet.packets.ranking.C2SRankingPersonalDataRequestPacket;
import com.jftse.emulator.server.game.core.packet.packets.ranking.S2CRankingDataAnswerPacket;
import com.jftse.emulator.server.game.core.packet.packets.ranking.S2CRankingPersonalDataAnswerPacket;
import com.jftse.emulator.server.game.core.packet.packets.shop.*;
import com.jftse.emulator.server.game.core.packet.packets.tutorial.C2STutorialBeginRequestPacket;
import com.jftse.emulator.server.game.core.packet.packets.tutorial.C2STutorialEndPacket;
import com.jftse.emulator.server.game.core.packet.packets.tutorial.S2CTutorialProgressAnswerPacket;
import com.jftse.emulator.server.game.core.service.*;
import com.jftse.emulator.server.game.core.service.ItemCharService;
import com.jftse.emulator.server.game.core.service.messaging.FriendService;
import com.jftse.emulator.server.game.core.service.messaging.GiftService;
import com.jftse.emulator.server.game.core.service.messaging.MessageService;
import com.jftse.emulator.server.game.core.service.messaging.ParcelService;
import com.jftse.emulator.server.game.core.singleplay.challenge.ChallengeBasicGame;
import com.jftse.emulator.server.game.core.singleplay.challenge.ChallengeBattleGame;
import com.jftse.emulator.server.game.core.singleplay.tutorial.TutorialGame;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;
import com.jftse.emulator.server.shared.module.GameHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
@Service
@RequiredArgsConstructor
public class GamePacketHandler {
    private final GameSessionManager gameSessionManager;
    private final GameHandler gameHandler;
    private final PacketEventHandler packetEventHandler;
    private final RunnableEventHandler runnableEventHandler;

    private final AuthenticationService authenticationService;
    private final PlayerService playerService;
    private final ClothEquipmentService clothEquipmentService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;
    private final ToolSlotEquipmentService toolSlotEquipmentService;
    private final SpecialSlotEquipmentService specialSlotEquipmentService;
    private final CardSlotEquipmentService cardSlotEquipmentService;
    private final PocketService pocketService;
    private final HomeService homeService;
    private final PlayerPocketService playerPocketService;
    private final ChallengeService challengeService;
    private final TutorialService tutorialService;
    private final ProductService productService;
    private final LotteryService lotteryService;
    private final ItemCharService itemCharService;
    private final PlayerStatisticService playerStatisticService;
    private final GuildMemberService guildMemberService;
    private final GuildService guildService;
    private final GuardianModeHandler guardianModeHandler;
    private final BasicModeHandler basicModeHandler;
    private final BattleModeHandler battleModeHandler;
    private final ClientWhitelistService clientWhitelistService;
    private final FriendService friendService;
    private final MessageService messageService;
    private final GiftService giftService;
    private final ParcelService parcelService;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        this.basicModeHandler.init(this.gameHandler);
        this.guardianModeHandler.init(this.gameHandler);
        this.battleModeHandler.init(this.gameHandler);
        scheduledExecutorService.scheduleAtFixedRate(packetEventHandler::handleQueuedPackets, 0, 5, TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                List<GameSession> gameSessions = new ArrayList<>();
                gameSessions.addAll(this.gameSessionManager.getGameSessionList()); // deep copy
                gameSessions.forEach(gameSession -> {
                    if (gameSession == null) return;
                    runnableEventHandler.handleQueuedRunnableEvents(gameSession);
                });
            } catch (Exception ex) {
                log.error(String.format("Exception in runnable thread: %s", ex.getMessage()));
            }
        }, 0, 5, TimeUnit.MILLISECONDS);
    }

    public GameHandler getGameHandler() {
        return gameHandler;
    }

    public void handleCleanUp() {
        // reset status
        this.getGameHandler().getClientList().forEach(c -> {
            Account account = c.getAccount();
            account.setStatus((int) S2CLoginAnswerPacket.SUCCESS);
            authenticationService.updateAccount(account);
        });

        if (GlobalSettings.IsAntiCheatEnabled) {
            List<ClientWhitelist> clientWhiteList = clientWhitelistService.findAll();
            for (int i = 0; i < clientWhiteList.size(); i++) {
                Long id = clientWhiteList.get(i).getId();
                clientWhitelistService.remove(id);
            }
        }

        this.getGameHandler().getRoomList().clear();
        this.getGameHandler().getClientList().clear();
        gameSessionManager.getGameSessionList().clear();
    }

    public void sendWelcomePacket(Connection connection) {
        if (connection.getRemoteAddressTCP() != null) {
            String hostAddress = connection.getRemoteAddressTCP().getAddress().getHostAddress();
            int port = connection.getRemoteAddressTCP().getPort();

            connection.getClient().setIp(hostAddress);
            connection.getClient().setPort(port);

            S2CWelcomePacket welcomePacket = new S2CWelcomePacket(0, 0, 0, 0);
            connection.sendTCP(welcomePacket);
        }
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
            connection.setHwid(gameServerLoginPacket.getHwid());

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

            S2CPlayerLevelExpPacket playerLevelExpPacket = new S2CPlayerLevelExpPacket(player.getLevel(), player.getExpPoints());
            connection.sendTCP(playerLevelExpPacket);

            player.setOnline(true);
            this.playerService.save(player);

            List<Friend> friends = this.friendService.findByPlayer(player).stream()
                    .filter(x -> x.getFriendshipState() == FriendshipState.Friends)
                    .collect(Collectors.toList());
            S2CFriendsListAnswerPacket s2CFriendsListAnswerPacket =
                    new S2CFriendsListAnswerPacket(friends);
            connection.sendTCP(s2CFriendsListAnswerPacket);
            friends.stream().filter(x -> x.getFriend().getOnline())
                    .forEach(x -> this.updateFriendsList(x.getFriend()));

            List<Friend> friendsWaitingForApproval = this.friendService.findByFriend(player).stream()
                    .filter(x -> x.getFriendshipState() == FriendshipState.WaitingApproval)
                    .collect(Collectors.toList());
            friendsWaitingForApproval.forEach(x -> {
                S2CFriendRequestNotificationPacket s2CFriendRequestNotificationPacket =
                        new S2CFriendRequestNotificationPacket(x.getPlayer().getName());
                connection.sendTCP(s2CFriendRequestNotificationPacket);
            });

            List<Message> messages = this.messageService.findByReceiver(player);
            messages.forEach(m -> {
                S2CReceivedMessageNotificationPacket s2CReceivedMessageNotificationPacket =
                        new S2CReceivedMessageNotificationPacket(m);
                connection.sendTCP(s2CReceivedMessageNotificationPacket);
            });

            List<Gift> gifts = this.giftService.findByReceiver(player);
            gifts.forEach(gift -> {
                S2CReceivedGiftNotificationPacket s2CReceivedGiftNotificationPacket =
                        new S2CReceivedGiftNotificationPacket(gift);
                connection.sendTCP(s2CReceivedGiftNotificationPacket);
            });

            List<Parcel> receivedParcels = this.parcelService.findByReceiver(player);
            receivedParcels.forEach(parcel -> {
                S2CReceivedParcelNotificationPacket s2CReceivedParcelNotificationPacket =
                        new S2CReceivedParcelNotificationPacket(parcel);
                connection.sendTCP(s2CReceivedParcelNotificationPacket);
            });

            List<Parcel> sentParcels = this.parcelService.findBySender(player);
            S2CSentParcelListPacket s2CSentParcelListPacket = new S2CSentParcelListPacket(sentParcels);
            connection.sendTCP(s2CSentParcelListPacket);

            GuildMember guildMember = this.guildMemberService.getByPlayer(player);
            if (guildMember != null) {
                Guild guild = guildMember.getGuild();
                if (guild != null) {
                    List<GuildMember> guildMembers = guild.getMemberList().stream()
                            .filter(x -> x != guildMember)
                            .collect(Collectors.toList());
                    S2CClubMembersListAnswerPacket s2CClubMembersListAnswerPacket =
                            new S2CClubMembersListAnswerPacket(guildMembers);
                    connection.sendTCP(s2CClubMembersListAnswerPacket);

                    guildMembers.stream().filter(x -> x.getPlayer().getOnline())
                            .forEach(x -> this.updateClubMembersList(x.getPlayer()));
                }
            }

            AccountHome accountHome = homeService.findAccountHomeByAccountId(account.getId());

            S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
            connection.sendTCP(homeDataPacket);
        }
        else if (requestType == 1) {
            S2CGameServerAnswerPacket gameServerAnswerPacket = new S2CGameServerAnswerPacket(requestType, (byte) 0);
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
            List<Integer> equippedToolSlots = toolSlotEquipmentService.getEquippedToolSlots(player);
            List<Integer> equippedSpecialSlots = specialSlotEquipmentService.getEquippedSpecialSlots(player);
            List<Integer> equippedCardSlots = cardSlotEquipmentService.getEquippedCardSlots(player);

            S2CPlayerStatusPointChangePacket playerStatusPointChangePacket = new S2CPlayerStatusPointChangePacket(player, statusPointsAddedDto);
            connection.sendTCP(playerStatusPointChangePacket);

            S2CPlayerInfoPlayStatsPacket playerInfoPlayStatsPacket = new S2CPlayerInfoPlayStatsPacket(player.getPlayerStatistic());
            connection.sendTCP(playerInfoPlayStatsPacket);

            S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, equippedCloths, player, statusPointsAddedDto);
            connection.sendTCP(inventoryWearClothAnswerPacket);

            S2CInventoryWearQuickAnswerPacket inventoryWearQuickAnswerPacket = new S2CInventoryWearQuickAnswerPacket(equippedQuickSlots);
            connection.sendTCP(inventoryWearQuickAnswerPacket);

            S2CInventoryWearToolAnswerPacket inventoryWearToolAnswerPacket = new S2CInventoryWearToolAnswerPacket(equippedToolSlots);
            connection.sendTCP(inventoryWearToolAnswerPacket);

            S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(equippedSpecialSlots);
            connection.sendTCP(inventoryWearSpecialAnswerPacket);

            S2CInventoryWearCardAnswerPacket inventoryWearCardAnswerPacket = new S2CInventoryWearCardAnswerPacket(equippedCardSlots);
            connection.sendTCP(inventoryWearCardAnswerPacket);
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

            if (inventoryItemId > 0) {
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
                    } else {
                        playerPocket.setItemCount(itemCount);
                        playerPocketService.save(playerPocket);
                    }

                    int itemIndex = (int) hidl.get("itemIndex");
                    byte unk0 = (byte) hidl.get("unk0");
                    byte rotation = (byte) hidl.get("rotation");
                    byte xPos = (byte) hidl.get("xPos");
                    byte yPos = (byte) hidl.get("yPos");

                    HomeInventory homeInventory = new HomeInventory();
                    homeInventory.setId((long) inventoryItemId);
                    homeInventory.setAccountHome(accountHome);
                    homeInventory.setItemIndex(itemIndex);
                    homeInventory.setUnk0(unk0);
                    homeInventory.setRotation(rotation);
                    homeInventory.setXPos(xPos);
                    homeInventory.setYPos(yPos);

                    homeInventory = homeService.save(homeInventory);

                    homeService.updateAccountHomeStatsByHomeInventory(accountHome, homeInventory, true);
                }
            }
            else if (inventoryItemId == -1) {
                // Not placed from player inventory but repositioned from home inventory
                int homeInventoryId = (int) hidl.get("homeInventoryId");
                int itemIndex = (int) hidl.get("itemIndex");
                byte unk0 = (byte) hidl.get("unk0");
                byte rotation = (byte) hidl.get("rotation");
                byte xPos = (byte) hidl.get("xPos");
                byte yPos = (byte) hidl.get("yPos");

                HomeInventory homeInventory = homeService.findById(homeInventoryId);
                if (homeInventory != null) {
                    homeInventory.setUnk0(unk0);
                    homeInventory.setRotation(rotation);
                    homeInventory.setXPos(xPos);
                    homeInventory.setYPos(yPos);
                    homeInventory = homeService.save(homeInventory);

                    homeService.updateAccountHomeStatsByHomeInventory(accountHome, homeInventory, true);
                }
            }
        });

        S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
        connection.sendTCP(homeDataPacket);
    }

    public void handleHomeItemClearRequestPacket(Connection connection, Packet packet) {

        AccountHome accountHome = homeService.findAccountHomeByAccountId(connection.getClient().getAccount().getId());
        List<HomeInventory> homeInventoryList = homeService.findAllByAccountHome(accountHome);

        homeInventoryList.forEach(hil -> {
                PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(hil.getItemIndex(), EItemCategory.HOUSE_DECO.getName(), connection.getClient().getActivePlayer().getPocket());
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

            List<Integer> itemsCount = IntStream.range(0, playerPocket.getItemCount()).boxed().collect(Collectors.toList());
            StreamUtils.batches(itemsCount, 500)
                .forEach(itemCount -> {
                    S2CInventorySellItemAnswerPacket inventorySellItemAnswerPacket = new S2CInventorySellItemAnswerPacket((char) itemCount.size(), itemPocketId);
                    connection.sendTCP(inventorySellItemAnswerPacket);
                });


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

    public void handleUnknownInventoryOpenPacket(Connection connection, Packet packet) {
        if (connection.getClient() != null) {
            Player player = connection.getClient().getActivePlayer();

            if (player != null) {
                StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
                Map<String, Integer> equippedCloths = clothEquipmentService.getEquippedCloths(player);
                List<Integer> equippedQuickSlots = quickSlotEquipmentService.getEquippedQuickSlots(player);
                List<Integer> equippedToolSlots = toolSlotEquipmentService.getEquippedToolSlots(player);
                List<Integer> equippedSpecialSlots = specialSlotEquipmentService.getEquippedSpecialSlots(player);
                List<Integer> equippedCardSlots = cardSlotEquipmentService.getEquippedCardSlots(player);

                S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, equippedCloths, player, statusPointsAddedDto);
                S2CInventoryWearQuickAnswerPacket inventoryWearQuickAnswerPacket = new S2CInventoryWearQuickAnswerPacket(equippedQuickSlots);
                S2CInventoryWearToolAnswerPacket inventoryWearToolAnswerPacket = new S2CInventoryWearToolAnswerPacket(equippedToolSlots);
                S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(equippedSpecialSlots);
                S2CInventoryWearCardAnswerPacket inventoryWearCardAnswerPacket = new S2CInventoryWearCardAnswerPacket(equippedCardSlots);

                connection.sendTCP(inventoryWearClothAnswerPacket);
                connection.sendTCP(inventoryWearQuickAnswerPacket);
                connection.sendTCP(inventoryWearToolAnswerPacket);
                connection.sendTCP(inventoryWearSpecialAnswerPacket);
                connection.sendTCP(inventoryWearCardAnswerPacket);
            }
        }

        Packet answer = new Packet((char) (packet.getPacketId() + 1));
        answer.write((char) 0);
        connection.sendTCP(answer);
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

        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            room.getRoomPlayerList().forEach(rp -> {
                if (rp.isFitting() && rp.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId())) {
                    rp.setClothEquipment(clothEquipmentService.findClothEquipmentById(clothEquipment.getId()));
                    rp.setStatusPointsAddedDto(statusPointsAddedDto);
                }
            });
        }

        S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, inventoryWearClothReqPacket, player, statusPointsAddedDto);
        connection.sendTCP(inventoryWearClothAnswerPacket);
    }

    public void handleInventoryWearToolPacket(Connection connection, Packet packet) {
        C2SInventoryWearToolRequestPacket inventoryWearToolRequestPacket = new C2SInventoryWearToolRequestPacket(packet);

        Player player = connection.getClient().getActivePlayer();
        ToolSlotEquipment toolSlotEquipment = player.getToolSlotEquipment();

        toolSlotEquipmentService.updateToolSlots(toolSlotEquipment, inventoryWearToolRequestPacket.getToolSlotList());
        player.setToolSlotEquipment(toolSlotEquipment);

        player = playerService.save(player);
        connection.getClient().setActivePlayer(player);

        S2CInventoryWearToolAnswerPacket inventoryWearToolAnswerPacket
                = new S2CInventoryWearToolAnswerPacket(inventoryWearToolRequestPacket.getToolSlotList());
        connection.sendTCP(inventoryWearToolAnswerPacket);
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

    public void handleInventoryWearSpecialPacket(Connection connection, Packet packet) {
        C2SInventoryWearSpecialRequestPacket inventoryWearSpecialRequestPacket = new C2SInventoryWearSpecialRequestPacket(packet);

        Player player = connection.getClient().getActivePlayer();
        SpecialSlotEquipment specialSlotEquipment = player.getSpecialSlotEquipment();

        specialSlotEquipmentService.updateSpecialSlots(specialSlotEquipment, inventoryWearSpecialRequestPacket.getSpecialSlotList());
        player.setSpecialSlotEquipment(specialSlotEquipment);

        player = playerService.save(player);
        connection.getClient().setActivePlayer(player);

        S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket
                = new S2CInventoryWearSpecialAnswerPacket(inventoryWearSpecialRequestPacket.getSpecialSlotList());
        connection.sendTCP(inventoryWearSpecialAnswerPacket);
    }

    public void handleInventoryWearCardPacket(Connection connection, Packet packet) {
        C2SInventoryWearCardRequestPacket inventoryWearCardRequestPacket = new C2SInventoryWearCardRequestPacket(packet);

        Player player = connection.getClient().getActivePlayer();
        CardSlotEquipment cardSlotEquipment = player.getCardSlotEquipment();

        cardSlotEquipmentService.updateCardSlots(cardSlotEquipment, inventoryWearCardRequestPacket.getCardSlotList());
        player.setCardSlotEquipment(cardSlotEquipment);

        player = playerService.save(player);
        connection.getClient().setActivePlayer(player);

        S2CInventoryWearCardAnswerPacket inventoryWearCardAnswerPacket
                = new S2CInventoryWearCardAnswerPacket(inventoryWearCardRequestPacket.getCardSlotList());
        connection.sendTCP(inventoryWearCardAnswerPacket);
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

                            if (option == 0)
                                playerPocket.setItemCount(product.getUse0() == 0 ? 1 : product.getUse0());
                            else if (option == 1)
                                playerPocket.setItemCount(product.getUse1());
                            else if (option == 2)
                                playerPocket.setItemCount(product.getUse2());

                            // no idea how itemCount can be null here, but ok
                            playerPocket.setItemCount((playerPocket.getItemCount() == null ? 0 : playerPocket.getItemCount()) + existingItemCount);

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
        String category = playerPocket.getCategory();
        int itemIndex = playerPocket.getItemIndex();

        if(category.equals("SPECIAL")  && itemIndex == 6 ){
            ItemChar itemChar = itemCharService.findByPlayerType(player.getPlayerType());
            player.setStrength(itemChar.getStrength());
            player.setStamina(itemChar.getStamina());
            player.setDexterity(itemChar.getDexterity());
            player.setWillpower(itemChar.getWillpower());
            player.setStatusPoints((byte) (player.getLevel() + 5 -1));
            StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
            S2CPlayerStatusPointChangePacket playerStatusPointChangePacket = new S2CPlayerStatusPointChangePacket(player, statusPointsAddedDto);
            connection.sendTCP(playerStatusPointChangePacket);
            S2CPlayerInfoPlayStatsPacket playerInfoPlayStatsPacket = new S2CPlayerInfoPlayStatsPacket(player.getPlayerStatistic());
            connection.sendTCP(playerInfoPlayStatsPacket);
        }
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
        byte clientLobbyCurrentPlayerListPage = connection.getClient().getLobbyCurrentPlayerListPage();
        boolean shouldJustRefresh = lobbyUserListRequestPacket.getRefresh() == 0 & page == 1;
        boolean wantsToGoBackOnNegativePage = page == -1 && clientLobbyCurrentPlayerListPage == 1;
        if (wantsToGoBackOnNegativePage || shouldJustRefresh) {
            page = 0;
        }

        clientLobbyCurrentPlayerListPage += page;
        connection.getClient().setLobbyCurrentPlayerListPage(clientLobbyCurrentPlayerListPage);
        List<Player> lobbyPlayerList = this.gameHandler.getPlayersInLobby().stream()
            .skip(clientLobbyCurrentPlayerListPage == 1 ? 0 : (clientLobbyCurrentPlayerListPage * 10) - 10)
            .limit(10)
            .collect(Collectors.toList());

        S2CLobbyUserListAnswerPacket lobbyUserListAnswerPacket = new S2CLobbyUserListAnswerPacket(lobbyPlayerList);
        connection.sendTCP(lobbyUserListAnswerPacket);
    }

    public void handleLobbyUserInfoReqPacket(Connection connection, Packet packet) {
        C2SLobbyUserInfoRequestPacket lobbyUserInfoRequestPacket = new C2SLobbyUserInfoRequestPacket(packet);

        Player player = playerService.findByIdFetched((long) lobbyUserInfoRequestPacket.getPlayerId());
        char result = (char) (player == null ? 1 : 0);

        GuildMember guildMember = guildMemberService.getByPlayer(player);
        Guild guild = null;
        if (guildMember != null && !guildMember.getWaitingForApproval() && guildMember.getGuild() != null)
            guild = guildMember.getGuild();

        S2CLobbyUserInfoAnswerPacket lobbyUserInfoAnswerPacket = new S2CLobbyUserInfoAnswerPacket(result, player, guild);
        connection.sendTCP(lobbyUserInfoAnswerPacket);
    }

    public void handleLobbyUserInfoClothReqPacket(Connection connection, Packet packet) {
        C2SLobbyUserInfoClothRequestPacket lobbyUserInfoClothRequestPacket = new C2SLobbyUserInfoClothRequestPacket(packet);

        Player player = playerService.findByIdFetched((long) lobbyUserInfoClothRequestPacket.getPlayerId());
        char result = (char) (player == null ? 1 : 0);

        S2CLobbyUserInfoClothAnswerPacket lobbyUserInfoClothAnswerPacket = new S2CLobbyUserInfoClothAnswerPacket(result, player);
        connection.sendTCP(lobbyUserInfoClothAnswerPacket);
    }

    public void handleChatMessagePackets(Connection connection, Packet packet) {
        switch (packet.getPacketId()) {
        case PacketID.C2SChatLobbyReq: {
            C2SChatLobbyReqPacket chatLobbyReqPacket = new C2SChatLobbyReqPacket(packet);
            S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket(chatLobbyReqPacket.getUnk(), connection.getClient().getActivePlayer().getName(), chatLobbyReqPacket.getMessage());

            List<Client> clientList = this.getGameHandler().getClientList().stream()
                    .filter(Client::isInLobby)
                    .collect(Collectors.toList());
            clientList.forEach(c -> c.getConnection().sendTCP(chatLobbyAnswerPacket));
        } break;
        case PacketID.C2SChatRoomReq: {
            C2SChatRoomReqPacket chatRoomReqPacket = new C2SChatRoomReqPacket(packet);
            S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket(chatRoomReqPacket.getType(), connection.getClient().getActivePlayer().getName(), chatRoomReqPacket.getMessage());

            Room room = connection.getClient().getActiveRoom();
            this.handleRoomChat(connection, room, chatRoomReqPacket, chatRoomAnswerPacket);
        } break;
        case PacketID.C2SWhisperReq: {
            C2SWhisperReqPacket whisperReqPacket = new C2SWhisperReqPacket(packet);
            S2CWhisperAnswerPacket whisperAnswerPacket = new S2CWhisperAnswerPacket(connection.getClient().getActivePlayer().getName(), whisperReqPacket.getReceiverName(), whisperReqPacket.getMessage());

            this.gameHandler.getClientList().stream()
                .filter(cl -> cl.getActivePlayer() != null && cl.getActivePlayer().getName().equalsIgnoreCase(whisperReqPacket.getReceiverName()))
                .findAny()
                .ifPresent(cl -> cl.getConnection().sendTCP(whisperAnswerPacket));

            connection.sendTCP(whisperAnswerPacket);
        } break;
        }
    }

    public void handleAddFriendRequestPacket(Connection connection, Packet packet) {
        C2SAddFriendRequestPacket c2SAddFriendRequestPacket =
                new C2SAddFriendRequestPacket(packet);
        Player player = connection.getClient().getActivePlayer();
        Player targetPlayer = this.playerService.findByName(c2SAddFriendRequestPacket.getPlayerName());
        if (targetPlayer == null) {
            S2CAddFriendResponsePacket s2CAddFriendResponsePacket =
                    new S2CAddFriendResponsePacket((short) -1);
            connection.sendTCP(s2CAddFriendResponsePacket);
            return;
        }

        List<Friend> friends = this.friendService.findByPlayer(player);
        Friend targetFriend = friends.stream()
                .filter(x -> x.getFriend().getId().equals(targetPlayer.getId()))
                .findFirst()
                .orElse(null);
        if (targetFriend == null) {
            Friend friend = new Friend();
            friend.setPlayer(player);
            friend.setFriend(targetPlayer);
            friend.setFriendshipState(FriendshipState.WaitingApproval);
            this.friendService.save(friend);
            S2CAddFriendResponsePacket s2CAddFriendResponsePacket =
                    new S2CAddFriendResponsePacket((short) 0);
            connection.sendTCP(s2CAddFriendResponsePacket);

            S2CFriendRequestNotificationPacket s2CFriendRequestNotificationPacket =
                    new S2CFriendRequestNotificationPacket(player.getName());
            Client friendClient = this.gameHandler.getClientList().stream()
                    .filter(x -> x.getActivePlayer().getId().equals(targetPlayer.getId()))
                    .findFirst()
                    .orElse(null);
            if (friendClient != null) {
                friendClient.getConnection().sendTCP(s2CFriendRequestNotificationPacket);
            }
            return;
        }

        if (targetFriend.getFriendshipState() == FriendshipState.Friends || targetFriend.getFriendshipState() == FriendshipState.Relationship) {
            S2CAddFriendResponsePacket s2CAddFriendResponsePacket =
                    new S2CAddFriendResponsePacket((short) -5);
            connection.sendTCP(s2CAddFriendResponsePacket);
            return;
        } else if (targetFriend.getFriendshipState() == FriendshipState.WaitingApproval) {
            S2CAddFriendResponsePacket s2CAddFriendResponsePacket =
                    new S2CAddFriendResponsePacket((short) -4);
            connection.sendTCP(s2CAddFriendResponsePacket);
            return;
        }
    }

    public void handleDeleteFriendRequest(Connection connection, Packet packet) {
        C2SDeleteFriendRequestPacket c2SDeleteFriendRequestPacket =
                new C2SDeleteFriendRequestPacket(packet);
        Player player = connection.getClient().getActivePlayer();
        Friend friend1 = this.friendService.findByPlayerIdAndFriendId(player.getId(), c2SDeleteFriendRequestPacket.getFriendId());
        if (friend1 != null) {
            this.friendService.remove(friend1.getId());
            S2CDeleteFriendResponsePacket s2CDeleteFriendResponsePacket =
                    new S2CDeleteFriendResponsePacket(friend1.getFriend());
            connection.sendTCP(s2CDeleteFriendResponsePacket);
        }

        Friend friend2 = this.friendService.findByPlayerIdAndFriendId(c2SDeleteFriendRequestPacket.getFriendId(), player.getId());
        if (friend2 != null) {
            this.friendService.remove(friend2.getId());
            this.updateFriendsList(friend2.getPlayer());
        }
    }

    public void handleAddFriendApprovalRequest(Connection connection, Packet packet) {
        C2SAddFriendApprovalRequestPacket c2SAddFriendApprovalRequestPacket =
                new C2SAddFriendApprovalRequestPacket(packet);
        Player targetPlayer = this.playerService.findByName(c2SAddFriendApprovalRequestPacket.getPlayerName());
        List<Friend> friends = this.friendService.findByPlayer(targetPlayer);
        Friend friend = friends.stream()
                .filter(x -> x.getFriend().getId().equals(connection.getClient().getActivePlayer().getId()))
                .findFirst()
                .orElse(null);
        if (friend == null) return;

        if (c2SAddFriendApprovalRequestPacket.isAccept()) {
            friend.setFriendshipState(FriendshipState.Friends);
            Friend newFriend = new Friend();
            newFriend.setPlayer(connection.getClient().getActivePlayer());
            newFriend.setFriend(targetPlayer);
            newFriend.setFriendshipState(FriendshipState.Friends);

            this.friendService.save(friend);
            this.friendService.save(newFriend);

            this.updateFriendsList(connection.getClient().getActivePlayer());
            this.updateFriendsList(targetPlayer);

            // TODO: ANSWER???
        } else {
            this.friendService.remove(friend.getId());
            // TODO: ANSWER???
        }
    }

    public void handleSendMessageRequest(Connection connection, Packet packet) {
        C2SSendMessageRequestPacket c2SSendMessageRequestPacket =
                new C2SSendMessageRequestPacket(packet);
        Player receiver = this.playerService.findByName(c2SSendMessageRequestPacket.getReceiverName());
        if (receiver != null) {
            Message message = new Message();
            message.setReceiver(receiver);
            message.setSender(connection.getClient().getActivePlayer());
            message.setMessage(c2SSendMessageRequestPacket.getMessage());
            message.setSeen(false);
            this.messageService.save(message);

            Client receiverClient = gameHandler.getClientList().stream()
                    .filter(x -> x.getActivePlayer().getId().equals(receiver.getId()))
                    .findFirst()
                    .orElse(null);
            if (receiverClient != null) {
                S2CReceivedMessageNotificationPacket s2CReceivedMessageNotificationPacket =
                        new S2CReceivedMessageNotificationPacket(message);
                receiverClient.getConnection().sendTCP(s2CReceivedMessageNotificationPacket);
            }
        }
    }

    public void handleMessageSeenRequest(Connection connection, Packet packet) {
        C2SMessageSeenRequestPacket c2SMessageSeenRequestPacket =
                new C2SMessageSeenRequestPacket(packet);
        if (c2SMessageSeenRequestPacket.getType() == 0) {
            Message message = this.messageService.findById(c2SMessageSeenRequestPacket.getMessageId().longValue());
            message.setSeen(true);
            this.messageService.save(message);
        } else if (c2SMessageSeenRequestPacket.getType() == 2) {
            Gift gift = this.giftService.findById(c2SMessageSeenRequestPacket.getMessageId().longValue());
            gift.setSeen(true);
            this.giftService.save(gift);
        }
    }

    public void handleSendGiftRequest(Connection connection, Packet packet) {
        C2SSendGiftRequestPacket c2SSendGiftRequestPacket = new C2SSendGiftRequestPacket(packet);
        Product product = this.productService.findProductByProductItemIndex(c2SSendGiftRequestPacket.getProductIndex());
        Player receiver = this.playerService.findByName(c2SSendGiftRequestPacket.getReceiverName());
        if (receiver != null && product != null) {
            Gift gift = new Gift();
            gift.setReceiver(receiver);
            gift.setSender(connection.getClient().getActivePlayer());
            gift.setMessage(c2SSendGiftRequestPacket.getMessage());
            gift.setSeen(false);
            gift.setProduct(product);
            this.giftService.save(gift);

            Client receiverClient = gameHandler.getClientList().stream()
                    .filter(x -> x.getActivePlayer().getId().equals(receiver.getId()))
                    .findFirst()
                    .orElse(null);
            if (receiverClient != null) {
                S2CReceivedGiftNotificationPacket s2CReceivedGiftNotificationPacket =
                        new S2CReceivedGiftNotificationPacket(gift);
                receiverClient.getConnection().sendTCP(s2CReceivedGiftNotificationPacket);
            }

            // 1. TODO: Actually buy and gift target player
            // 2. TODO: Handle all cases
            // 0 = Item purchase successful, -1 = Not enough gold, -2 = Not enough AP,
            // -3 = Receiver reached maximum number of character, -6 = That user already has the maximum number of this item
            // -8 = That users character model cannot equip this item,  -9 = You cannot send gifts purchases with gold to that character
            S2CSendGiftAnswerPacket s2CSendGiftAnswerPacket = new S2CSendGiftAnswerPacket((short) 0, gift);
            connection.sendTCP(s2CSendGiftAnswerPacket);
        }
    }

    public void handleSendParcelRequest(Connection connection, Packet packet) {
        C2SSendParcelRequestPacket c2SSendParcelRequestPacket = new C2SSendParcelRequestPacket(packet);
        PlayerPocket item = this.playerPocketService.findById(c2SSendParcelRequestPacket.getPlayerPocketId().longValue());
        Player sender = connection.getClient().getActivePlayer();
        Player receiver = this.playerService.findByName(c2SSendParcelRequestPacket.getReceiverName());
        if (receiver != null && item != null) {
            if (item != null) {
                // TODO: Parcels should have a retention of 7days. -> After 7 days delete parcels and return items back to senders pocket.
                Parcel parcel = new Parcel();
                parcel.setReceiver(receiver);
                parcel.setSender(connection.getClient().getActivePlayer());
                parcel.setMessage(c2SSendParcelRequestPacket.getMessage());
                parcel.setGold(c2SSendParcelRequestPacket.getCashOnDelivery());

                parcel.setItemCount(item.getItemCount());
                parcel.setCategory(item.getCategory());
                parcel.setItemIndex(item.getItemIndex());
                parcel.setUseType(item.getUseType());

                // TODO: Is this right?
                if (receiver.getId().equals(sender.getId())) {
                    parcel.setParcelType(ParcelType.Gold);
                } else {
                    parcel.setParcelType(ParcelType.CashOnDelivery);
                }

                this.parcelService.save(parcel);
                this.playerPocketService.remove(item.getId());

                Client receiverClient = gameHandler.getClientList().stream()
                        .filter(x -> x.getActivePlayer().getId().equals(receiver.getId()))
                        .findFirst()
                        .orElse(null);
                if (receiverClient != null) {
                    S2CReceivedParcelNotificationPacket s2CReceivedParcelNotificationPacket =
                            new S2CReceivedParcelNotificationPacket(parcel);
                    receiverClient.getConnection().sendTCP(s2CReceivedParcelNotificationPacket);
                }

                // TODO: Handle fee
                // TODO: Handle all these cases
                // 0 = Successfully sent
                //-1 = Failed to send parcel
                //-2 = You do not have enough gold
                //-4 = Under level 20 user can not send parcel
                //-5 = Gold transactions must be under 1.000.000
                S2CSendParcelAnswerPacket s2CSendParcelAnswerPacket = new S2CSendParcelAnswerPacket((short) 0);
                connection.sendTCP(s2CSendParcelAnswerPacket);

                S2CInventoryItemRemoveAnswerPacket s2CInventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(item.getId().intValue());
                connection.sendTCP(s2CInventoryItemRemoveAnswerPacket);
            }
        }
    }

    public void handleDenyParcelRequest(Connection connection, Packet packet) {
        C2SDenyParcelRequest c2SDenyParcelRequest = new C2SDenyParcelRequest(packet);
        Parcel parcel = this.parcelService.findById(c2SDenyParcelRequest.getParcelId().longValue());
        PlayerPocket item = this.playerPocketService.getItemAsPocketByItemIndexAndPocket(parcel.getItemIndex(), parcel.getSender().getPocket());
        if (item == null) {
            item = new PlayerPocket();
            item.setCategory(parcel.getCategory());
            item.setItemCount(parcel.getItemCount());
            item.setItemIndex(parcel.getItemIndex());
            item.setUseType(parcel.getUseType());
            item.setPocket(parcel.getSender().getPocket());
        } else {
            item.setItemCount(item.getItemCount() + parcel.getItemCount());
        }

        this.playerPocketService.save(item);
        this.parcelService.remove(parcel.getId());

        S2CRemoveParcelFromListPacket s2CRemoveParcelFromListPacket = new S2CRemoveParcelFromListPacket(parcel.getId().intValue());
        connection.sendTCP(s2CRemoveParcelFromListPacket);

        List<PlayerPocket> items = this.playerPocketService.getPlayerPocketItems(parcel.getSender().getPocket());
        Client senderClient = gameHandler.getClientList().stream()
                .filter(x -> x.getActivePlayer().getId().equals(parcel.getSender().getId()))
                .findFirst()
                .orElse(null);
        if (senderClient != null) {
            S2CInventoryDataPacket s2CInventoryDataPacket = new S2CInventoryDataPacket(items);
            senderClient.getConnection().sendTCP(s2CInventoryDataPacket);
        }
    }

    public void handleAcceptParcelRequest(Connection connection, Packet packet) {
        C2SAcceptParcelRequest c2SAcceptParcelRequest = new C2SAcceptParcelRequest(packet);
        Parcel parcel = this.parcelService.findById(c2SAcceptParcelRequest.getParcelId().longValue());

        // TODO: Check if enough money?
        Player receiver = parcel.getReceiver();
        Integer newGoldReceiver = receiver.getGold() - parcel.getGold();
        receiver.setGold(newGoldReceiver);

        Player sender = parcel.getSender();
        Integer newGoldSender = sender.getGold() + parcel.getGold();
        sender.setGold(newGoldSender);

        Pocket receiverPocket = receiver.getPocket();
        PlayerPocket item = this.playerPocketService.getItemAsPocketByItemIndexAndPocket(parcel.getItemIndex(), receiverPocket);
        if (item == null) {
            item = new PlayerPocket();
            item.setCategory(parcel.getCategory());
            item.setItemCount(parcel.getItemCount());
            item.setItemIndex(parcel.getItemIndex());
            item.setUseType(parcel.getUseType());
            item.setPocket(receiverPocket);
        } else {
            item.setItemCount(item.getItemCount() + parcel.getItemCount());
        }

        this.playerPocketService.save(item);
        this.parcelService.remove(parcel.getId());
        this.playerService.save(receiver);
        this.playerService.save(sender);

        Client senderClient = gameHandler.getClientList().stream()
                .filter(x -> x.getActivePlayer().getId().equals(sender.getId()))
                .findFirst()
                .orElse(null);
        if (senderClient != null) {
            S2CShopMoneyAnswerPacket senderMoneyPacket = new S2CShopMoneyAnswerPacket(sender);
            senderClient.getConnection().sendTCP(senderMoneyPacket);
        }

        S2CRemoveParcelFromListPacket s2CRemoveParcelFromListPacket = new S2CRemoveParcelFromListPacket(parcel.getId().intValue());
        connection.sendTCP(s2CRemoveParcelFromListPacket);

        S2CShopMoneyAnswerPacket receiverMoneyPacket = new S2CShopMoneyAnswerPacket(receiver);
        connection.sendTCP(receiverMoneyPacket);

        List<PlayerPocket> items = this.playerPocketService.getPlayerPocketItems(receiver.getPocket());
        S2CInventoryDataPacket s2CInventoryDataPacket = new S2CInventoryDataPacket(items);
        connection.sendTCP(s2CInventoryDataPacket);
    }

    public void handleDeleteMessageRequest(Connection connection, Packet packet) {
        C2SDeleteMessagesRequest c2SDeleteMessagesRequest = new C2SDeleteMessagesRequest(packet);
        if (c2SDeleteMessagesRequest.getType() == 0) {
            c2SDeleteMessagesRequest.getMessageIds().forEach(m -> {
                this.messageService.remove(m.longValue());
            });
        } else if (c2SDeleteMessagesRequest.getType() == 2) {
            c2SDeleteMessagesRequest.getMessageIds().forEach(m -> {
                this.giftService.remove(m.longValue());
            });
        }
    }

    private void updateFriendsList(Player player) {
        List<Friend> friends = this.friendService.findByPlayer(player).stream()
                .filter(x -> x.getFriendshipState() == FriendshipState.Friends)
                .collect(Collectors.toList());
        S2CFriendsListAnswerPacket s2CFriendsListAnswerPacket =
                new S2CFriendsListAnswerPacket(friends);
        Client client = this.gameHandler.getClientList().stream()
                .filter(x -> x.getActivePlayer().getId().equals(player.getId()))
                .findFirst()
                .orElse(null);
        if (client != null) {
            client.getConnection().sendTCP(s2CFriendsListAnswerPacket);
        }
    }

    private void updateClubMembersList(Player player) {
        GuildMember guildMember = this.guildMemberService.getByPlayer(player);
        if (guildMember != null) {
            Guild guild = guildMember.getGuild();
            if (guild != null) {
                List<GuildMember> guildMembers = guild.getMemberList().stream()
                        .filter(x -> x != guildMember)
                        .collect(Collectors.toList());
                S2CClubMembersListAnswerPacket s2CClubMembersListAnswerPacket =
                        new S2CClubMembersListAnswerPacket(guildMembers);
                Client client = this.gameHandler.getClientList().stream()
                        .filter(x -> x.getActivePlayer().getId().equals(player.getId()))
                        .findFirst()
                        .orElse(null);
                if (client != null) {
                    client.getConnection().sendTCP(s2CClubMembersListAnswerPacket);
                }
            }
        }
    }

    public void handleLobbyJoinLeave(Connection connection, boolean joined) {
        connection.getClient().setInLobby(joined);
        connection.getClient().setLobbyCurrentRoomListPage((short) -1);

        if (joined) {
            handleRoomPlayerChanges(connection);
        }

        this.refreshLobbyPlayerListForAllClients();
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

    public void handleRoomCreateRequestPacket(Connection connection, Packet packet) {
        // prevent multiple room creations, this might have to be adjusted into a "room join answer"
        if (connection.getClient() != null && connection.getClient().getActiveRoom() != null)
            return;

        C2SRoomCreateRequestPacket roomCreateRequestPacket = new C2SRoomCreateRequestPacket(packet);

        Room room = new Room();
        room.setRoomId(this.getRoomId());
        room.setRoomName(roomCreateRequestPacket.getRoomName());
        room.setAllowBattlemon((byte) 0);

        room.setMode(roomCreateRequestPacket.getMode());
        room.setRule(roomCreateRequestPacket.getRule());
        room.setPlayers(roomCreateRequestPacket.getPlayers());
        room.setPrivate(roomCreateRequestPacket.isPrivate());
        room.setPassword(roomCreateRequestPacket.getPassword());
        room.setUnk1(roomCreateRequestPacket.getUnk1());
        room.setSkillFree(roomCreateRequestPacket.isSkillFree());
        room.setQuickSlot(roomCreateRequestPacket.isQuickSlot());
        room.setLevel(connection.getClient().getActivePlayer().getLevel());
        room.setLevelRange(roomCreateRequestPacket.getLevelRange());
        room.setBettingType(roomCreateRequestPacket.getBettingType());
        room.setBettingAmount(roomCreateRequestPacket.getBettingAmount());
        room.setBall(roomCreateRequestPacket.getBall());
        room.setMap((byte) 1);

        internalHandleRoomCreate(connection, room);
    }

    public void handleRoomCreateQuickRequestPacket(Connection connection, Packet packet) {
        // prevent multiple room creations, this might have to be adjusted into a "room join answer"
        if (connection.getClient() != null && connection.getClient().getActiveRoom() != null)
            return;

        C2SRoomCreateQuickRequestPacket roomQuickCreateRequestPacket = new C2SRoomCreateQuickRequestPacket(packet);
        if (roomQuickCreateRequestPacket.getMode() == GameMode.BATTLEMON)
            return;

        Player player = connection.getClient().getActivePlayer();
        byte playerSize = roomQuickCreateRequestPacket.getPlayers();

        Room room = new Room();
        room.setRoomId(this.getRoomId());
        room.setRoomName(String.format("%s's room", player.getName()));
        room.setAllowBattlemon(roomQuickCreateRequestPacket.getAllowBattlemon());

        room.setMode(roomQuickCreateRequestPacket.getMode());
        room.setRule((byte) 0);

        if (roomQuickCreateRequestPacket.getMode() == GameMode.GUARDIAN)
            room.setPlayers((byte) 4);
        else
            room.setPlayers(playerSize == 0 ? 2 : playerSize);

        room.setPrivate(false);
        room.setUnk1((byte) 0);
        room.setSkillFree(false);
        room.setQuickSlot(false);
        room.setLevel(player.getLevel());
        room.setLevelRange((byte) -1);
        room.setBettingType('0');
        room.setBettingAmount(0);
        room.setBall(1);
        room.setMap((byte) 1);

        internalHandleRoomCreate(connection, room);
    }

    public void handleRoomNameChangePacket(Connection connection, Packet packet) {
        C2SRoomNameChangeRequestPacket changeRoomNameRequestPacket = new C2SRoomNameChangeRequestPacket(packet);
        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            room.setRoomName(changeRoomNameRequestPacket.getRoomName());
            S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
            this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(roomInformationPacket));
        }
    }

    public void handleGameModeChangePacket(Connection connection, Packet packet) {
        C2SRoomGameModeChangeRequestPacket changeRoomGameModeRequestPacket = new C2SRoomGameModeChangeRequestPacket(packet);
        Room room = connection.getClient().getActiveRoom();

        if (changeRoomGameModeRequestPacket.getMode() == GameMode.BATTLE) {
            changeRoomGameModeRequestPacket.setMode((byte) GameMode.GUARDIAN);
        }

        if (room != null) {
            room.setMode(changeRoomGameModeRequestPacket.getMode());
            S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
            this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(roomInformationPacket));

            this.gameHandler.getClientsInLobby().forEach(c -> {
                boolean isActivePlayer = c.getActivePlayer().getId().equals(connection.getClient().getActivePlayer().getId());
                if (isActivePlayer)
                    return;

                S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(this.getFilteredRoomsForClient(c));
                c.getConnection().sendTCP(roomListAnswerPacket);
            });
        }
    }

    public void handleRoomIsPrivateChangePacket(Connection connection, Packet packet) {
        C2SRoomIsPrivateChangeRequestPacket changeRoomIsPrivateRequestPacket = new C2SRoomIsPrivateChangeRequestPacket(packet);
        String password = changeRoomIsPrivateRequestPacket.getPassword();
        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            if (StringUtils.isEmpty(password)) {
                room.setPassword(null);
                room.setPrivate(false);
            } else {
                room.setPassword(password);
                room.setPrivate(true);
            }

            S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
            this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(roomInformationPacket));
        }
    }

    public void handleRoomLevelRangeChangePacket(Connection connection, Packet packet) {
        C2SRoomLevelRangeChangeRequestPacket changeRoomLevelRangeRequestPacket = new C2SRoomLevelRangeChangeRequestPacket(packet);
        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            room.setLevelRange(changeRoomLevelRangeRequestPacket.getLevelRange());

            S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
            this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(roomInformationPacket));
        }
    }

    public void handleRoomSkillFreeChangePacket(Connection connection, Packet packet) {
        C2SRoomSkillFreeChangeRequestPacket changeRoomSkillFreeRequestPacket = new C2SRoomSkillFreeChangeRequestPacket(packet);
        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            room.setSkillFree(changeRoomSkillFreeRequestPacket.isSkillFree());

            S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
            this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(roomInformationPacket));
        }
    }

    public void handleRoomAllowBattlemonChangePacket(Connection connection, Packet packet) {
        C2SRoomAllowBattlemonChangeRequestPacket changeRoomAllowBattlemonRequestPacket = new C2SRoomAllowBattlemonChangeRequestPacket(packet);
        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            byte allowBattlemon = changeRoomAllowBattlemonRequestPacket.getAllowBattlemon() == 1 ? (byte) 2 : (byte) 0;
            // disable battlemon
            room.setAllowBattlemon((byte) 0);

            S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
            this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(roomInformationPacket));
        }
    }

    public void handleRoomQuickSlotChangePacket(Connection connection, Packet packet) {
        C2SRoomQuickSlotChangeRequestPacket changeRoomQuickSlotRequestPacket = new C2SRoomQuickSlotChangeRequestPacket(packet);
        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            room.setQuickSlot(changeRoomQuickSlotRequestPacket.isQuickSlot());

            S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
            this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(roomInformationPacket));
        }
    }

    public void handleRoomJoinRequestPacket(Connection connection, Packet packet) {
        List<Room> roomList = new ArrayList<>(this.gameHandler.getRoomList());
        C2SRoomJoinRequestPacket roomJoinRequestPacket = new C2SRoomJoinRequestPacket(packet, roomList);

        Room room = roomList.stream()
                .filter(r -> r.getRoomId() == roomJoinRequestPacket.getRoomId())
                .findAny()
                .orElse(null);

        // prevent abusive room joins
        if (room != null && connection.getClient() != null && connection.getClient().getActiveRoom() != null) {
            Room clientRoom = connection.getClient().getActiveRoom();

            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) 0, (byte) 0, (byte) 0, (byte) 0);
            S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(clientRoom);

            connection.sendTCP(roomJoinAnswerPacket);
            connection.sendTCP(roomInformationPacket);

            List<Short> positions = clientRoom.getPositions();
            for (int i = 0; i < positions.size(); i++) {
                short positionState = clientRoom.getPositions().get(i);
                if (positionState == RoomPositionState.Locked) {
                    S2CRoomSlotCloseAnswerPacket roomSlotCloseAnswerPacket = new S2CRoomSlotCloseAnswerPacket((byte) i, true);
                    connection.sendTCP(roomSlotCloseAnswerPacket);
                }
            }

            List<RoomPlayer> roomPlayerList = clientRoom.getRoomPlayerList();
            S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(roomPlayerList);
            this.gameHandler.getClientsInRoom(clientRoom.getRoomId()).forEach(c -> c.getConnection().sendTCP(roomPlayerInformationPacket));
            this.updateRoomForAllPlayersInMultiplayer(connection, clientRoom);
            this.refreshLobbyPlayerListForAllClients();

            return;
        }

        if (room == null) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(roomList);
            connection.sendTCP(roomListAnswerPacket);
            return;
        }

        if (room.getStatus() != RoomStatus.NotRunning) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -1, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            this.updateRoomForAllPlayersInMultiplayer(connection, room);
            return;
        }

        if (room.isPrivate() && (StringUtils.isEmpty(roomJoinRequestPacket.getPassword()) || !roomJoinRequestPacket.getPassword().equals(room.getPassword()))) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -5, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            this.updateRoomForAllPlayersInMultiplayer(connection, room);
            return;
        }

        boolean anyPositionAvailable = room.getPositions().stream().anyMatch(x -> x == RoomPositionState.Free);
        if (!anyPositionAvailable) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            this.updateRoomForAllPlayersInMultiplayer(connection, room);
            return;
        }

        Player activePlayer = connection.getClient().getActivePlayer();
        if ((room.isHardMode() || room.isArcade()) && activePlayer.getLevel() != 60) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            this.updateRoomForAllPlayersInMultiplayer(connection, room);
            return;
        }

        Optional<Short> num = room.getPositions().stream().filter(x -> x == RoomPositionState.Free).findFirst();
        int newPosition = room.getPositions().indexOf(num.get());
        room.getPositions().set(newPosition, RoomPositionState.InUse);

        RoomPlayer roomPlayer = new RoomPlayer();
        roomPlayer.setPlayer(activePlayer);
        roomPlayer.setGuildMember(guildMemberService.getByPlayer(activePlayer));
        roomPlayer.setClothEquipment(clothEquipmentService.findClothEquipmentById(roomPlayer.getPlayer().getClothEquipment().getId()));
        roomPlayer.setStatusPointsAddedDto(clothEquipmentService.getStatusPointsFromCloths(roomPlayer.getPlayer()));
        roomPlayer.setPosition((short) newPosition);
        roomPlayer.setMaster(false);
        roomPlayer.setFitting(false);
        room.getRoomPlayerList().add(roomPlayer);

        connection.getClient().setActiveRoom(room);
        connection.getClient().setInLobby(false);

        S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) 0, (byte) 0, (byte) 0, (byte) 0);
        connection.sendTCP(roomJoinAnswerPacket);

        S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
        connection.sendTCP(roomInformationPacket);

        List<Short> positions = room.getPositions();
        for (int i = 0; i < positions.size(); i++) {
            short positionState = room.getPositions().get(i);
            if (positionState == RoomPositionState.Locked) {
                S2CRoomSlotCloseAnswerPacket roomSlotCloseAnswerPacket = new S2CRoomSlotCloseAnswerPacket((byte) i, true);
                connection.sendTCP(roomSlotCloseAnswerPacket);
            }
        }

        List<RoomPlayer> roomPlayerList = room.getRoomPlayerList();
        S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(roomPlayerList);
        this.gameHandler.getClientsInRoom(roomJoinRequestPacket.getRoomId()).forEach(c -> c.getConnection().sendTCP(roomPlayerInformationPacket));
        this.updateRoomForAllPlayersInMultiplayer(connection, room);
        this.refreshLobbyPlayerListForAllClients();
    }

    public void handleRoomLeaveRequestPacket(Connection connection, Packet packet) {
        connection.getClient().setLobbyCurrentRoomListPage((short) -1);
        handleRoomPlayerChanges(connection);
        Packet answerPacket = new Packet(PacketID.S2CRoomLeaveAnswer);
        answerPacket.write(0);
        connection.sendTCP(answerPacket);
    }

    public void handleRoomReadyChangeRequestPacket(Connection connection, Packet packet) {
        C2SRoomReadyChangeRequestPacket roomReadyChangeRequestPacket = new C2SRoomReadyChangeRequestPacket(packet);

        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            room.getRoomPlayerList().stream()
                    .filter(rp -> rp.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                    .findAny()
                    .ifPresent(rp -> rp.setReady(roomReadyChangeRequestPacket.isReady()));

            List<RoomPlayer> roomPlayerList = room.getRoomPlayerList();
            S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(roomPlayerList);
            this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(roomPlayerInformationPacket));
        }
    }

    public void handleRoomMapChangeRequestPacket(Connection connection, Packet packet) {
        C2SRoomMapChangeRequestPacket roomMapChangeRequestPacket = new C2SRoomMapChangeRequestPacket(packet);
        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            room.setMap(roomMapChangeRequestPacket.getMap());
            S2CRoomMapChangeAnswerPacket roomMapChangeAnswerPacket = new S2CRoomMapChangeAnswerPacket(roomMapChangeRequestPacket.getMap());
            this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(roomMapChangeAnswerPacket));
        }
    }

    public void handleRoomPositionChangeRequestPacket(Connection connection, Packet packet) {
        C2SRoomPositionChangeRequestPacket roomPositionChangeRequestPacket = new C2SRoomPositionChangeRequestPacket(packet);
        short positionToClaim = roomPositionChangeRequestPacket.getPosition();

        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            RoomPlayer requestingSlotChangePlayer = room.getRoomPlayerList().stream()
                    .filter(rp -> rp.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                    .findAny()
                    .orElse(null);

            if (requestingSlotChangePlayer != null) {
                short requestingSlotChangePlayerOldPosition = requestingSlotChangePlayer.getPosition();
                if (requestingSlotChangePlayerOldPosition == positionToClaim) {
                    return;
                }

                boolean requestingSlotChangePlayerIsMaster = requestingSlotChangePlayer.isMaster();
                boolean slotIsInUse = connection.getClient().getActiveRoom().getPositions().get(positionToClaim) == RoomPositionState.InUse;
                if (slotIsInUse && !requestingSlotChangePlayerIsMaster) {
                    S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", "You cannot claim this players slot");
                    connection.sendTCP(chatRoomAnswerPacket);
                    return;
                }

                boolean freeOldPosition = true;
                RoomPlayer playerInSlotToClaim = room.getRoomPlayerList().stream().filter(x -> x.getPosition() == positionToClaim).findAny().orElse(null);
                if (playerInSlotToClaim != null) {
                    freeOldPosition = false;
                    this.internalHandleRoomPositionChange(connection, playerInSlotToClaim, false,
                            playerInSlotToClaim.getPosition(), requestingSlotChangePlayerOldPosition);
                }

                this.internalHandleRoomPositionChange(connection, requestingSlotChangePlayer, freeOldPosition,
                        requestingSlotChangePlayerOldPosition, positionToClaim);
            }

            List<RoomPlayer> roomPlayerList = connection.getClient().getActiveRoom().getRoomPlayerList();
            roomPlayerList.forEach(x -> x.setReady(false));
            S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(roomPlayerList);
            this.gameHandler.getClientsInRoom(connection.getClient().getActiveRoom().getRoomId()).forEach(c -> c.getConnection().sendTCP(roomPlayerInformationPacket));
        }
    }

    public void handleRoomKickPlayerRequestPacket(Connection connection, Packet packet) {
        C2SRoomKickPlayerRequestPacket roomKickPlayerRequestPacket = new C2SRoomKickPlayerRequestPacket(packet);
        Room room = connection.getClient().getActiveRoom();

        if (room != null) {
            List<Client> clientsInRoom = this.gameHandler.getClientsInRoom(room.getRoomId());
            RoomPlayer playerToKick = room.getRoomPlayerList().stream()
                    .filter(rp -> rp.getPosition() == roomKickPlayerRequestPacket.getPosition())
                    .findAny()
                    .orElse(null);

            if (playerToKick != null) {
                Client client = clientsInRoom.stream()
                        .filter(x -> x.getActivePlayer().getId().equals(playerToKick.getPlayer().getId()))
                        .findFirst().orElse(null);
                if (client != null) {
                    Packet answerPacket = new Packet(PacketID.S2CRoomLeaveAnswer);
                    answerPacket.write(0);
                    client.getConnection().sendTCP(answerPacket);

                    handleRoomPlayerChanges(client.getConnection());

                    S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -4, (byte) 0, (byte) 0, (byte) 0);
                    client.getConnection().sendTCP(roomJoinAnswerPacket);
                }
            }
        }
    }

    public void handleRoomSlotCloseRequestPacket(Connection connection, Packet packet) {
        C2SRoomSlotCloseRequestPacket roomSlotCloseRequestPacket = new C2SRoomSlotCloseRequestPacket(packet);
        boolean deactivate = roomSlotCloseRequestPacket.isDeactivate();

        byte slot = roomSlotCloseRequestPacket.getSlot();
        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            room.getPositions().set(slot, deactivate ? RoomPositionState.Locked : RoomPositionState.Free);

            S2CRoomSlotCloseAnswerPacket roomSlotCloseAnswerPacket = new S2CRoomSlotCloseAnswerPacket(slot, deactivate);
            this.gameHandler.getClientsInRoom(connection.getClient().getActiveRoom().getRoomId()).forEach(c -> c.getConnection().sendTCP(roomSlotCloseAnswerPacket));
        }
    }

    public void handleRoomFittingRequestPacket(Connection connection, Packet packet) {
        C2SRoomFittingRequestPacket roomFittingRequestPacket = new C2SRoomFittingRequestPacket(packet);
        boolean fitting = roomFittingRequestPacket.isFitting();

        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            room.getRoomPlayerList().forEach(rp -> {
                if (rp.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                    rp.setFitting(fitting);
            });

            S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(room.getRoomPlayerList());
            this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null)
                    c.getConnection().sendTCP(roomPlayerInformationPacket);
            });
        }
    }

    public void handleRoomStartGamePacket(Connection connection, Packet packet) {
        Packet roomStartGameAck = new Packet(PacketID.S2CRoomStartGameAck);
        roomStartGameAck.write((char) 0);

        Room room = connection.getClient().getActiveRoom();
        if (room == null) {
            connection.sendTCP(roomStartGameAck);
            return;
        }

        if (room.getStatus() == RoomStatus.StartingGame) {
            connection.sendTCP(roomStartGameAck);
            room.setStatus(RoomStatus.StartCancelled);
            return;
        }

        if (room.getStatus() != RoomStatus.NotRunning) {
            connection.sendTCP(roomStartGameAck);
            return;
        }

        room.setStatus(RoomStatus.StartingGame);

        GameServer relayServer = authenticationService.getGameServerByPort(connection.getServer().getTcpPort() + 1);

        List<Client> clientsInRoom = new ArrayList<>(Collections.unmodifiableList(this.gameHandler.getClientsInRoom(connection.getClient().getActiveRoom().getRoomId())));

        GameSession gameSession = new GameSession();
        gameSession.setSessionId(room.getRoomId());
        gameSession.setPlayers(room.getPlayers());
        switch (room.getMode()) {
            case GameMode.BASIC:
                gameSession.setActiveMatchplayGame(new MatchplayBasicGame(room.getPlayers()));
                break;
            case GameMode.BATTLE:
                gameSession.setActiveMatchplayGame(new MatchplayBattleGame());
                break;
            case GameMode.GUARDIAN:
                gameSession.setActiveMatchplayGame(new MatchplayGuardianGame());
                break;
        }

        clientsInRoom.forEach(c -> c.setActiveGameSession(gameSession));

        gameSession.setClients(new ConcurrentLinkedDeque<>(clientsInRoom));
        this.gameSessionManager.addGameSession(gameSession);

        List<Client> clientInRoomLeftShiftList = new ArrayList<>(clientsInRoom);
        clientsInRoom.forEach(c -> {
            Packet unsetHostPacket = new Packet(PacketID.S2CUnsetHost);
            unsetHostPacket.write((byte) 0);
            c.getConnection().sendTCP(unsetHostPacket);

            S2CGameNetworkSettingsPacket gameNetworkSettings = new S2CGameNetworkSettingsPacket(relayServer.getHost(), relayServer.getPort(), room, clientInRoomLeftShiftList);
            c.getConnection().sendTCP(gameNetworkSettings);

            // shift list to the left, so every client has his player id in the first place when doing session register
            clientInRoomLeftShiftList.add(0, clientInRoomLeftShiftList.remove(clientInRoomLeftShiftList.size() - 1));
        });

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(() -> {
            int secondsToCount = 5;
            for (int i = 0; i < secondsToCount; i++) {
                Room threadRoom = connection.getClient().getActiveRoom();
                List<Room> roomList = new ArrayList<>(this.gameHandler.getRoomList());
                Room allPlayerRoom = roomList.stream()
                        .filter(r -> r.getRoomId() == room.getRoomId())
                        .findAny()
                        .orElse(null);
                if (allPlayerRoom != null) {
                    boolean allReady = allPlayerRoom.getRoomPlayerList().stream()
                            .filter(rp -> !rp.isMaster())
                            .collect(Collectors.toList())
                            .stream()
                            .filter(rp -> rp.getPosition() < 4)
                            .allMatch(RoomPlayer::isReady);

                    if (!allReady || threadRoom.getStatus() == RoomStatus.StartCancelled) {
                        threadRoom.setStatus(RoomStatus.NotRunning);
                        Packet startGameCancelledPacket = new Packet(PacketID.S2CRoomStartGameCancelled);
                        startGameCancelledPacket.write((char) 0);

                        allPlayerRoom.getRoomPlayerList().stream()
                                .filter(rp -> !rp.isMaster())
                                .forEach(rp -> rp.setReady(false));
                        clientsInRoom.forEach(c -> c.setActiveGameSession(null));

                        this.gameSessionManager.removeGameSession(gameSession);

                        S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(allPlayerRoom.getRoomPlayerList());
                        this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(roomPlayerInformationPacket));
                        this.updateRoomForAllPlayersInMultiplayer(connection, room);
                        this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(startGameCancelledPacket));
                        return;
                    }
                }

                String message = String.format("Game starting in %s...", secondsToCount - i);
                S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", message);
                this.gameHandler.getClientsInRoom(threadRoom.getRoomId()).forEach(c -> c.getConnection().sendTCP(chatRoomAnswerPacket));
                try {
                    TimeUnit.MILLISECONDS.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            RoomPlayer playerInSlot0 = room.getRoomPlayerList().stream()
                    .filter(x -> x.getPosition() == 0)
                    .findFirst().orElse(null);
            Client clientToHostGame = gameHandler.getClientsInRoom(room.getRoomId()).stream()
                    .filter(x -> playerInSlot0 != null && x.getActivePlayer().getId().equals(playerInSlot0.getPlayer().getId()))
                    .findFirst()
                    .orElse(connection.getClient());
            Packet setHostPacket = new Packet(PacketID.S2CSetHost);
            setHostPacket.write((byte) 1);
            clientToHostGame.getConnection().sendTCP(setHostPacket);

            Packet setHostUnknownPacket = new Packet(PacketID.S2CSetHostUnknown);
            clientToHostGame.getConnection().sendTCP(setHostUnknownPacket);

            switch (room.getMode()) {
                case GameMode.BATTLE:
                    this.battleModeHandler.handlePrepareBattleMode(connection, room);
                    break;
                case GameMode.GUARDIAN:
                    this.guardianModeHandler.handlePrepareGuardianMode(connection, room);
                    break;
            }
            
            Packet startGamePacket = new Packet(PacketID.S2CRoomStartGame);
            startGamePacket.write((char) 0);
            room.setStatus(RoomStatus.InitializingGame);
            this.gameHandler.getClientsInRoom(connection.getClient().getActiveRoom().getRoomId())
                    .forEach(c -> c.getConnection().sendTCP(startGamePacket));

        }, 0, TimeUnit.SECONDS);

        connection.sendTCP(roomStartGameAck);
        executor.shutdown();
    }

    public void handleGameAnimationReadyToSkipPacket(Connection connection, Packet packet) {
        Player player = connection.getClient().getActivePlayer();
        Room room = connection.getClient().getActiveRoom();
        room.getRoomPlayerList().stream()
            .filter(x -> x.getPlayer().getId().equals(player.getId()))
            .findFirst()
                .ifPresent(rp -> rp.setGameAnimationSkipReady(true));

        Room allPlayerRoom = this.gameHandler.getRoomList().stream()
                .filter(r -> r.getRoomId() == room.getRoomId())
                .findAny()
                .orElse(null);
        if (allPlayerRoom != null) {
            boolean allPlayerCanSkipAnimation = allPlayerRoom.getRoomPlayerList().stream()
                    .allMatch(RoomPlayer::isGameAnimationSkipReady);

            if (allPlayerCanSkipAnimation) {
                Packet gameAnimationAllowSkipPacket = new Packet(PacketID.S2CGameAnimationAllowSkip);
                gameAnimationAllowSkipPacket.write((char) 0);
                this.gameHandler.getClientsInRoom(connection.getClient().getActiveRoom().getRoomId())
                        .forEach(c -> c.getConnection().sendTCP(gameAnimationAllowSkipPacket));
            }
        }
    }

    public  void handleGameAnimationSkipTriggeredPacket(Connection connection, Packet packet) {
        Room room = connection.getClient().getActiveRoom();
        List<RoomPlayer> roomPlayerList = connection.getClient().getActiveRoom().getRoomPlayerList();
        Optional<RoomPlayer> roomPlayer = roomPlayerList.stream()
                .filter(x -> x.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                .findFirst();

        if (room.getStatus() != RoomStatus.InitializingGame) {
            return;
        }

        if (roomPlayer.isPresent()) {
            Packet gameAnimationSkipPacket = new Packet(PacketID.S2CGameAnimationSkip);
            gameAnimationSkipPacket.write((char) 0);
            sendPacketToAllInRoom(connection, gameAnimationSkipPacket);

            S2CGameDisplayPlayerStatsPacket playerStatsPacket = new S2CGameDisplayPlayerStatsPacket(connection.getClient().getActiveRoom());
            sendPacketToAllInRoom(connection, playerStatsPacket);
            room.setStatus(RoomStatus.Running);

            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
            executor.schedule(() -> {
                Client client = connection.getClient();
                if (client == null) return;

                Room threadRoom = client.getActiveRoom();
                if (threadRoom == null || threadRoom.getStatus() != RoomStatus.Running) {
                    return;
                }

                S2CGameSetNameColorAndRemoveBlackBar setNameColorAndRemoveBlackBarPacket = new S2CGameSetNameColorAndRemoveBlackBar(room);
                sendPacketToAllInRoom(connection, setNameColorAndRemoveBlackBarPacket);

                switch (room.getMode()) {
                    case GameMode.BASIC:
                        this.basicModeHandler.handleStartBasicMode(connection, room, roomPlayerList);
                        break;
                    case GameMode.BATTLE:
                        this.battleModeHandler.handleStartBattleMode(connection, room);
                        break;
                    case GameMode.GUARDIAN:
                        this.guardianModeHandler.handleStartGuardianMode(connection, room);
                        break;
                }
            }, 8, TimeUnit.SECONDS);
            executor.shutdown();
        }
    }

    public void handleRoomListRequestPacket(Connection connection, Packet packet) {
        C2SRoomListRequestPacket roomListRequestPacket = new C2SRoomListRequestPacket(packet);

        int roomType = roomListRequestPacket.getRoomTypeTab();
        int gameMode;
        switch (roomType) {
            case 256:
                gameMode = GameMode.GUARDIAN;
                break;
            case 192:
                gameMode = GameMode.BATTLE;
                break;
            case 48:
                gameMode = GameMode.BASIC;
                break;
            case 1536:
                gameMode = GameMode.BATTLEMON;
                break;
            default:
                gameMode = GameMode.ALL;
                break;
        }

        short direction = roomListRequestPacket.getDirection() == 0 ? (short) -1 : (short) 1;
        short currentLobbyRoomListPage = connection.getClient().getLobbyCurrentRoomListPage();

        boolean wantsToGoBackOnNegativePage = direction == -1 && currentLobbyRoomListPage == 0;
        if (wantsToGoBackOnNegativePage) {
            direction = 0;
        }

        int currentRoomType = connection.getClient().getLobbyGameModeTabFilter();
        int availableRoomsCount = (int) this.gameHandler.getRoomList().stream()
                .filter(x -> currentRoomType == GameMode.ALL || getRoomMode(x) == currentRoomType)
                .count();

        int possibleRoomsDisplayed = (currentLobbyRoomListPage + 1) * 5;
        if (direction == -1 || availableRoomsCount > possibleRoomsDisplayed) {
            currentLobbyRoomListPage += direction;
        }

        if (currentRoomType != gameMode || currentLobbyRoomListPage < 0) {
            currentLobbyRoomListPage = 0;
        }

        connection.getClient().setLobbyCurrentRoomListPage(currentLobbyRoomListPage);

        connection.getClient().setLobbyGameModeTabFilter(gameMode);
        int finalGameMode = gameMode;
        List<Room> roomList = this.gameHandler.getRoomList().stream()
                .filter(x -> finalGameMode == GameMode.ALL || getRoomMode(x) == finalGameMode)
                .skip(currentLobbyRoomListPage * 5)
                .limit(5)
                .collect(Collectors.toList());

        S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(roomList);
        connection.sendTCP(roomListAnswerPacket);
    }

    public void handleDevPacket(Connection connection, Packet packet) {
        if (GlobalSettings.ShouldHandleDevPackets) {
            byte[] data = packet.getData();
            Packet packetToRelay = new Packet(data);
            this.getGameHandler().getClientList().forEach(x -> x.getConnection().sendTCP(packetToRelay));
        }
    }

    public void handleMatchplayPointPacket(Connection connection, Packet packet) {
        C2SMatchplayPointPacket matchplayPointPacket = new C2SMatchplayPointPacket(packet);

        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession != null) {
            MatchplayGame game = connection.getClient().getActiveGameSession().getActiveMatchplayGame();
            if (game instanceof MatchplayBasicGame) {
                this.basicModeHandler.handleBasicModeMatchplayPointPacket(connection, matchplayPointPacket, gameSession, (MatchplayBasicGame) game);
            } else if (game instanceof MatchplayGuardianGame) {
                this.guardianModeHandler.handleGuardianModeMatchplayPointPacket(connection, matchplayPointPacket, gameSession, (MatchplayGuardianGame) game);
            } else if (game instanceof MatchplayBattleGame) {
                this.battleModeHandler.handleBattleModeMatchplayPointPacket(connection, matchplayPointPacket, gameSession, (MatchplayBattleGame) game);
            }
        }
    }

    public void handleGuildNoticeRequestPacket(Connection connection, Packet packet) {
        Player activePlayer = connection.getClient().getActivePlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);
        if (guildMember != null)
            connection.sendTCP(new S2CGuildNoticeAnswerPacket(guildMember.getGuild().getNotice()));
    }

    public void handleGuildNameCheckRequestPacket(Connection connection, Packet packet) {
        C2SGuildNameCheckRequestPacket guildNameCheckRequestPacket = new C2SGuildNameCheckRequestPacket(packet);

        if (guildService.findByName(guildNameCheckRequestPacket.getName()) != null)
            connection.sendTCP(new S2CGuildNameCheckAnswerPacket((short) -1));
        else
            connection.sendTCP(new S2CGuildNameCheckAnswerPacket((short) 0));
    }

    public void handleGuildCreateRequestPacket(Connection connection, Packet packet) {
        C2SGuildCreateRequestPacket guildCreateRequestPacket = new C2SGuildCreateRequestPacket(packet);

        String guildName = guildCreateRequestPacket.getName();
        if (guildName.length() < 2 || guildName.length() > 12 ||  this.guildService.findByName(guildName) != null) {
            connection.sendTCP(new S2CGuildCreateAnswerPacket((char) -1)); // This name cannot be used as a Club name.
            return;
        }

        Player activePlayer = connection.getClient().getActivePlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);
        if (guildMember != null) {
            connection.sendTCP(new S2CGuildCreateAnswerPacket((char) -2)); // You already have a Club.
            return;
        }
        else if (activePlayer.getGold() < 5000) {
            connection.sendTCP(new S2CGuildCreateAnswerPacket((char) -3)); // You do not have enough gold to create a new Club
            return;
        }
        else if (activePlayer.getLevel() < 10) {
            connection.sendTCP(new S2CGuildCreateAnswerPacket((char) -4)); // Your level is too low to create a new Club.
            return;
        }

        Guild guild = new Guild();
        guild.setName(guildName);
        guild.setIntroduction(guildCreateRequestPacket.getIntroduction());
        guild.setIsPublic(guildCreateRequestPacket.isPublic());
        guild.setLevelRestriction(guildCreateRequestPacket.getLevelRestriction());
        guild.setAllowedCharacterType(guildCreateRequestPacket.getAllowedCharacterType());
        this.guildService.save(guild);

        guildMember = new GuildMember();
        guildMember.setGuild(guild);
        guildMember.setPlayer(connection.getClient().getActivePlayer());
        guildMember.setMemberRank((byte) 3); // ClubMaster
        guildMember.setRequestDate(new Date());
        guildMember.setWaitingForApproval(false);
        this.guildMemberService.save(guildMember);

        activePlayer.setGold(activePlayer.getGold() - 5000);
        this.playerService.save(activePlayer);

        guild.setMemberList(Arrays.asList(guildMember));
        connection.sendTCP(new S2CGuildCreateAnswerPacket((char) 0));
    }

    public void handleGuildDataRequestPacket(Connection connection, Packet packet) {
        Player activePlayer = connection.getClient().getActivePlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember == null)
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -2, null));
        else if (guildMember.getWaitingForApproval())
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -1, guildMember.getGuild()));
        else
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) 0, guildMember.getGuild()));
    }

    public void handleGuildListRequestPacket(Connection connection, Packet packet) {
        C2SGuildListRequestPacket guildListRequestPacket = new C2SGuildListRequestPacket(packet);
        if (guildListRequestPacket.getPage() == 0) {
            List<Guild> guildList = this.guildService.findAll();
            StreamUtils.batches(guildList, 10).forEach(guilds -> connection.sendTCP(new S2CGuildListAnswerPacket(guilds)));
        }
    }

    public void handleGuildJoinRequestPacket(Connection connection, Packet packet) {
        C2SGuildJoinRequestPacket guildJoinRequestPacket = new C2SGuildJoinRequestPacket(packet);
        Player activePlayer = connection.getClient().getActivePlayer();

        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);
        if (guildMember != null && guildMember.getWaitingForApproval()) {
            connection.sendTCP(new S2CGuildJoinAnswerPacket((short) -3));
            return;
        }

        if (guildMember != null) {
            connection.sendTCP(new S2CGuildJoinAnswerPacket((short) -2));
            return;
        }

        Guild guild = guildService.findById((long)guildJoinRequestPacket.getGuildId());
        if (guild == null) {
            connection.sendTCP(new S2CGuildJoinAnswerPacket((short) -1));
            return;
        }

        if (guild.getMemberList().stream().filter(x -> !x.getWaitingForApproval()).count() >= guild.getMaxMemberCount()) {
            connection.sendTCP(new S2CGuildJoinAnswerPacket((short) -7));
            return;
        }

        if (activePlayer.getLevel() < guild.getLevelRestriction()) {
            connection.sendTCP(new S2CGuildJoinAnswerPacket((short) -4));
            return;
        }

        boolean characterAllowed = false;
        for (byte type : guild.getAllowedCharacterType()) {
            characterAllowed = type == activePlayer.getPlayerType();
            if (characterAllowed) break;
        }

        if (!characterAllowed) {
            connection.sendTCP(new S2CGuildJoinAnswerPacket((short) -5));
            return;
        }

        guildMember = new GuildMember();
        guildMember.setGuild(guild);
        guildMember.setPlayer(activePlayer);
        guildMember.setMemberRank((byte) 1);
        guildMember.setRequestDate(new Date());
        guildMember.setWaitingForApproval(!guild.getIsPublic());
        guildMemberService.save(guildMember);

        if (guild.getIsPublic()) {
            connection.sendTCP(new S2CGuildJoinAnswerPacket((short) 1));
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) 0, guild));
        }
        else
            connection.sendTCP(new S2CGuildJoinAnswerPacket((short) 0));
    }

    public void handleGuildLeaveRequestPacket(Connection connection, Packet packet) {
        Player activePlayer = connection.getClient().getActivePlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);
        Guild guild = guildMember.getGuild();
        guild.getMemberList().removeIf(x -> x.getId().equals(guildMember.getId()));
        guildService.save(guild);

        connection.sendTCP(new S2CGuildDataAnswerPacket((short) -2, guild));
    }

    public void handleGuildChangeInformationRequestPacket(Connection connection, Packet packet) {
        C2SGuildChangeInformationRequestPacket guildChangeInformationRequestPacket =
                new C2SGuildChangeInformationRequestPacket(packet);

        Player activePlayer = connection.getClient().getActivePlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getMemberRank() > 1) {
            Guild guild = guildMember.getGuild();

            guild.setIntroduction(guildChangeInformationRequestPacket.getIntroduction());
            guild.setLevelRestriction(guildChangeInformationRequestPacket.getMinLevel());
            guild.setIsPublic(guildChangeInformationRequestPacket.isPublic());
            guild.setAllowedCharacterType(guildChangeInformationRequestPacket.getAllowedCharacterType());
            guildService.save(guild);
        }

        connection.sendTCP(new S2CGuildDataAnswerPacket((byte) 0, guildMember.getGuild()));
    }

    public void handleGuildReverseMemberDataRequestPacket(Connection connection, Packet packet) {
        C2SGuildReserveMemberDataRequestPacket c2SGuildReserveMemberDataRequestPacket =
                new C2SGuildReserveMemberDataRequestPacket(packet);
        if (c2SGuildReserveMemberDataRequestPacket.getPage() != 0) return;

        Player activePlayer = connection.getClient().getActivePlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        List<GuildMember> reverseMemberList = guildMember.getGuild()
                .getMemberList().stream().filter(GuildMember::getWaitingForApproval).collect(Collectors.toList());
        connection.sendTCP(new S2CGuildReverseMemberAnswerPacket(reverseMemberList));
    }

    public void handleGuildMemberDataRequestPacket(Connection connection, Packet packet) {
        C2SGuildMemberDataRequestPacket c2SGuildMemberDataRequestPacket =
                new C2SGuildMemberDataRequestPacket(packet);
        Player activePlayer = connection.getClient().getActivePlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && c2SGuildMemberDataRequestPacket.getPage() == 0) {
            List<GuildMember> guildMembers = guildMember.getGuild().getMemberList()
                    .stream()
                    .filter(x -> !x.getWaitingForApproval())
                    .collect(Collectors.toList());
            connection.sendTCP(new S2CGuildMemberDataAnswerPacket(guildMembers));
        }
    }

    public void handleGuildChangeMasterRequestPacket(Connection connection, Packet packet) {
        C2SGuildChangeMasterRequestPacket guildChangeMasterRequestPacket
                = new C2SGuildChangeMasterRequestPacket(packet);

        Player activePlayer = connection.getClient().getActivePlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getMemberRank() == 3) {
            GuildMember newClubMaster = this.getGuildMemberByPlayerPositionInGuild(
                    guildChangeMasterRequestPacket.getPlayerPositionInGuild(),
                    guildMember);

            if (newClubMaster != null) {
                guildMember.setMemberRank((byte) 2);
                guildMemberService.save(guildMember);

                newClubMaster.setMemberRank((byte) 3);
                guildMemberService.save(newClubMaster);

                connection.sendTCP(new S2CGuildChangeMasterAnswerPacket((short) 0));
            }
        }
        else
            connection.sendTCP(new S2CGuildChangeMasterAnswerPacket((short) -1));
    }

    public void handleGuildChangeSubMasterRequestPacket(Connection connection, Packet packet) {
        C2SGuildChangeSubMasterRequestPacket guildChangeSubMasterRequestPacket
                = new C2SGuildChangeSubMasterRequestPacket(packet);

        Player activePlayer = connection.getClient().getActivePlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getMemberRank() == 3) {
            GuildMember subClubMaster = this.getGuildMemberByPlayerPositionInGuild(
                    guildChangeSubMasterRequestPacket.getPlayerPositionInGuild(),
                    guildMember);
            if (subClubMaster != null) {
                Guild guild = subClubMaster.getGuild();
                long subMasterCount = guild.getMemberList()
                        .stream()
                        .filter(x -> !x.getWaitingForApproval() && x.getMemberRank() == 2)
                        .count();

                if (guildChangeSubMasterRequestPacket.getStatus() == 1) {
                    if (subMasterCount == 3) {
                        connection.sendTCP(new S2CGuildChangeSubMasterAnswerPacket((byte) 0, (short) -5));
                    } else {
                        subClubMaster.setMemberRank((byte) 2);
                        guildMemberService.save((subClubMaster));
                        connection.sendTCP(new S2CGuildChangeSubMasterAnswerPacket((byte) 1, (short) 0));
                    }
                }
                else {
                    subClubMaster.setMemberRank((byte) 1);
                    guildMemberService.save((subClubMaster));
                    connection.sendTCP(new S2CGuildChangeSubMasterAnswerPacket((byte) 0, (short) 0));
                }
            }
        }
        else
            connection.sendTCP(new S2CGuildChangeSubMasterAnswerPacket((byte) 0, (short) -1));
    }

    public void handleGuildDismissMemberRequestPacket(Connection connection, Packet packet) {
        C2SGuildDismissMemberRequestPacket guildDismissMemberRequestPacket
                = new C2SGuildDismissMemberRequestPacket(packet);

        Player activePlayer = connection.getClient().getActivePlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getMemberRank() > 1) {
            GuildMember dismissMember = this.getGuildMemberByPlayerPositionInGuild(
                    guildDismissMemberRequestPacket.getPlayerPositionInGuild(),
                    guildMember);
            if (dismissMember != null) {
                if (dismissMember.getMemberRank() == 3) {
                    S2CGuildDismissMemberAnswerPacket answerPacketForDismissedMember = new S2CGuildDismissMemberAnswerPacket((short) -5);
                    connection.sendTCP(answerPacketForDismissedMember);
                } else {
                    Client targetClient = gameHandler.getClientList()
                            .stream()
                            .filter(x -> x.getActivePlayer().getId().equals(dismissMember.getPlayer().getId()))
                            .findFirst()
                            .orElse(null);

                    Guild guild = dismissMember.getGuild();
                    guild.getMemberList().removeIf(x -> x.getId().equals(dismissMember.getId()));
                    guildService.save(guild);

                    if (targetClient != null) {
                        S2CGuildDismissMemberAnswerPacket answerPacketForDismissedMember = new S2CGuildDismissMemberAnswerPacket((short) 0);
                        targetClient.getConnection().sendTCP(answerPacketForDismissedMember);
                    }
                }
            }
        }
    }

    public void handleGuildDeleteRequestPacket(Connection connection, Packet packet) {
        Player activePlayer = connection.getClient().getActivePlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getMemberRank() == 3) {
            Guild guild = guildMember.getGuild();
            guildService.remove(guild.getId());
            connection.sendTCP(new S2CGuildDeleteAnswerPacket((short) 0));
        }
    }

    public void handleGuildChangeNoticeRequestPacket(Connection connection, Packet packet) {
        C2SGuildChangeNoticeRequestPacket guildChangeNoticeRequestPacket
                = new C2SGuildChangeNoticeRequestPacket(packet);

        Player activePlayer = connection.getClient().getActivePlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getMemberRank() > 1) {
            Guild guild = guildMember.getGuild();
            guild.setNotice(guildChangeNoticeRequestPacket.getNotice());
            guildService.save(guild);
        }
    }

    public void handleGuildChatRequestPacket(Connection connection, Packet packet) {
        C2SGuildChatRequestPacket guildChatRequestPacket = new C2SGuildChatRequestPacket(packet);
        Player activePlayer = connection.getClient().getActivePlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);
        List<GuildMember> guildMembers = guildMember.getGuild().getMemberList()
                .stream()
                .filter(x -> !x.getWaitingForApproval())
                .collect(Collectors.toList());
        List<Integer> allPlayerIds = guildMembers.stream()
                .map(x -> x.getPlayer().getId().intValue())
                .collect(Collectors.toList());
        List<Client> allClients = gameHandler.getClientList().stream()
                .filter(c -> allPlayerIds.contains(c.getActivePlayer().getId().intValue()))
                .collect(Collectors.toList());
        allClients.forEach(c -> c.getConnection().sendTCP(new S2CGuildChatAnswerPacket(activePlayer.getName(), guildChatRequestPacket.getMessage())));
    }

    public void handleGuildSearchRequestPacket(Connection connection, Packet packet) {
        C2SGuildSearchRequestPacket guildSearchRequestPacket = new C2SGuildSearchRequestPacket(packet);
        byte searchType = guildSearchRequestPacket.getSearchType();

        switch (searchType) {
            case 0:
                Guild guild = guildService.findById((long) guildSearchRequestPacket.getNumber());
                if (guild != null)
                    connection.sendTCP(new S2CGuildSearchAnswerPacket(Collections.singletonList(guild)));
                else
                    connection.sendTCP(new S2CGuildSearchAnswerPacket(new ArrayList<>()));
                break;

            case 1:
                List<Guild> guildList = new ArrayList<>(guildService.findAllByNameContaining(guildSearchRequestPacket.getName()));
                StreamUtils.batches(guildList, 10).forEach(guilds -> connection.sendTCP(new S2CGuildSearchAnswerPacket(guilds)));
                break;

            default:
                break;
        }
    }

    public void handleGuildChangeReverseMemberRequest(Connection connection, Packet packet) {
        C2SGuildChangeReverseMemberRequestPacket guildChangeReverseMemberRequestPacket
                = new C2SGuildChangeReverseMemberRequestPacket(packet);

        Player activePlayer = connection.getClient().getActivePlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getMemberRank() > 1) {
            GuildMember reverseMember = guildMember.getGuild()
                    .getMemberList().stream()
                    .filter(gm -> gm.getPlayer().getId() == guildChangeReverseMemberRequestPacket.getPlayerId())
                    .findFirst().orElse(null);

            if (reverseMember != null) {
                if (guildChangeReverseMemberRequestPacket.getStatus() == 1) {
                    reverseMember.setWaitingForApproval(false);
                    guildMemberService.save(reverseMember);
                    connection.sendTCP(new S2CGuildChangeReverseMemberAnswerPacket((byte) 1, (short) 0));
                }
                else {
                    reverseMember.getGuild().getMemberList().removeIf(x -> x.getId().equals(reverseMember.getId()));
                    guildService.save(reverseMember.getGuild());
                    connection.sendTCP(new S2CGuildChangeReverseMemberAnswerPacket((byte) 0, (short) 0));
                }
            }
        }
        else
            connection.sendTCP(new S2CGuildChangeReverseMemberAnswerPacket((byte) 0, (short) -4));
    }

    public void handleGuildChangeLogoRequest(Connection connection, Packet packet) {
        C2SGuildChangeLogoRequestPacket c2SGuildChangeLogoRequestPacket =
                new C2SGuildChangeLogoRequestPacket(packet);

        Player player = connection.getClient().getActivePlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(player);

        if (guildMember.getMemberRank() == 3) {
            if (c2SGuildChangeLogoRequestPacket.getPocketIdLogoBackground() > 0) {
                PlayerPocket backgroundImagePocket = playerPocketService.findById((long) c2SGuildChangeLogoRequestPacket.getPocketIdLogoBackground());
                guildMember.getGuild().setLogoBackgroundId(backgroundImagePocket.getItemIndex());
                guildMember.getGuild().setLogoBackgroundColor(c2SGuildChangeLogoRequestPacket.getLogoBackgroundColor());
            } else {
                guildMember.getGuild().setLogoBackgroundId(-1);
                guildMember.getGuild().setLogoBackgroundColor(-1);
            }

            if (c2SGuildChangeLogoRequestPacket.getPocketIdLogoPattern() > 0) {
                PlayerPocket patternPocket = playerPocketService.findById((long) c2SGuildChangeLogoRequestPacket.getPocketIdLogoPattern());
                guildMember.getGuild().setLogoPatternId(patternPocket.getItemIndex());
                guildMember.getGuild().setLogoPatternColor(c2SGuildChangeLogoRequestPacket.getLogoPatternColor());
            } else {
                guildMember.getGuild().setLogoPatternId(-1);
                guildMember.getGuild().setLogoPatternColor(-1);
            }

            if (c2SGuildChangeLogoRequestPacket.getPocketIdLogoMark() > 0) {
                PlayerPocket markPocket = playerPocketService.findById((long) c2SGuildChangeLogoRequestPacket.getPocketIdLogoMark());
                guildMember.getGuild().setLogoMarkId(markPocket.getItemIndex());
                guildMember.getGuild().setLogoMarkColor(c2SGuildChangeLogoRequestPacket.getLogoMarkColor());
            } else {
                guildMember.getGuild().setLogoMarkId(-1);
                guildMember.getGuild().setLogoMarkColor(-1);
            }

            guildService.save(guildMember.getGuild());

            S2CGuildChangeLogoAnswerPacket answer = new S2CGuildChangeLogoAnswerPacket((short) 0);
            connection.sendTCP(answer);
        } else
            connection.sendTCP(new S2CGuildChangeLogoAnswerPacket((short) -2));
    }

    public void handleDisconnectPacket(Connection connection, Packet packet) {
        if (connection.getClient().getAccount() != null) {
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
        }

        S2CDisconnectAnswerPacket disconnectAnswerPacket = new S2CDisconnectAnswerPacket();
        connection.sendTCP(disconnectAnswerPacket);
    }

    public void handleDisconnected(Connection connection) {
        if (connection.getClient().getAccount() != null) {
            // reset status
            Account account = authenticationService.findAccountById(connection.getClient().getAccount().getId());
            account.setStatus((int) S2CLoginAnswerPacket.SUCCESS);
            authenticationService.updateAccount(account);
            Player player = connection.getClient().getActivePlayer();
            if (player != null) {
                player = this.playerService.findById(player.getId());
                player.setOnline(false);
                this.playerService.save(player);
                connection.getClient().setActivePlayer(player);
                List<Friend> friends = this.friendService.findByPlayer(player);
                friends.forEach(x -> this.updateFriendsList(x.getFriend()));

                GuildMember guildMember = this.guildMemberService.getByPlayer(player);
                if (guildMember != null && guildMember.getGuild() != null) {
                    guildMember.getGuild().getMemberList().stream()
                            .filter(x -> x != guildMember)
                            .forEach(x -> this.updateClubMembersList(x.getPlayer()));
                }
            }

            GameSession gameSession = connection.getClient().getActiveGameSession();
            if (gameSession != null) {
                Room currentClientRoom = connection.getClient().getActiveRoom();

                if (currentClientRoom != null) {
                    if (player != null && currentClientRoom.getStatus() == RoomStatus.Running) {
                        PlayerStatistic playerStatistic = player.getPlayerStatistic();
                        playerStatistic.setNumberOfDisconnects(playerStatistic.getNumberOfDisconnects() + 1);
                        playerStatistic = playerStatisticService.save(player.getPlayerStatistic());

                        player.setPlayerStatistic(playerStatistic);
                        player = playerService.save(player);
                        connection.getClient().setActivePlayer(player);
                    }

                    currentClientRoom.setStatus(RoomStatus.NotRunning);

                    gameSession.getClients().forEach(c -> {
                        Room room = c.getActiveRoom();
                        if (room != null) {
                            if (c.getConnection().getId() != connection.getId()) {
                                S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
                                c.getConnection().sendTCP(backToRoomPacket);
                            }
                        }
                    });
                    this.gameSessionManager.getGameSessionList().removeIf(gs -> gs.getSessionId() == gameSession.getSessionId());

                    connection.getClient().setActiveGameSession(null);

                    Room room = this.gameHandler.getRoomList().stream()
                            .filter(r -> r.getRoomId() == currentClientRoom.getRoomId())
                            .findAny()
                            .orElse(null);
                    if (room != null) {
                        this.gameHandler.getRoomList().removeFirstOccurrence(room);
                        this.gameHandler.getRoomList().add(currentClientRoom);
                    }
                }
            }
            handleRoomPlayerChanges(connection);
        }

        gameHandler.removeClient(connection.getClient());
        connection.close();
    }

    public void handleClientBackInRoomPacket(Connection connection, Packet packet) {
        Room currentClientRoom = connection.getClient().getActiveRoom();
        if (currentClientRoom == null) { // shouldn't happen
            S2CDisconnectAnswerPacket disconnectAnswerPacket = new S2CDisconnectAnswerPacket();
            connection.sendTCP(disconnectAnswerPacket);
            return;
        }

        short position = currentClientRoom.getRoomPlayerList().stream()
                .filter(rp -> rp.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                .findAny()
                .get()
                .getPosition();

        Packet backInRoomAckPacket = new Packet(PacketID.S2CMatchplayClientBackInRoomAck);
        backInRoomAckPacket.write(position);
        connection.sendTCP(backInRoomAckPacket);

        Packet unsetHostPacket = new Packet(PacketID.S2CUnsetHost);
        unsetHostPacket.write((byte) 0);
        connection.sendTCP(unsetHostPacket);

        this.gameHandler.getRoomList().stream()
                .filter(r -> r.getRoomId() == currentClientRoom.getRoomId())
                .findAny()
                .ifPresent(r -> r.getRoomPlayerList().forEach(rp -> rp.setReady(false)));
        this.gameHandler.getRoomList().stream()
                .filter(r -> r.getRoomId() == currentClientRoom.getRoomId())
                .findAny()
                .ifPresent(r -> r.setStatus(RoomStatus.NotRunning));

        Player player = connection.getClient().getActivePlayer();
        PlayerStatistic playerStatistic = playerStatisticService.findPlayerStatisticById(player.getPlayerStatistic().getId());
        player.setPlayerStatistic(playerStatistic);
        player = playerService.save(player);
        connection.getClient().setActivePlayer(player);

        StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);

        S2CPlayerStatusPointChangePacket playerStatusPointChangePacket = new S2CPlayerStatusPointChangePacket(player, statusPointsAddedDto);
        S2CPlayerInfoPlayStatsPacket playerInfoPlayStatsPacket = new S2CPlayerInfoPlayStatsPacket(playerStatistic);
        S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(currentClientRoom);
        S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(currentClientRoom.getRoomPlayerList());
        connection.sendTCP(playerStatusPointChangePacket);
        connection.sendTCP(playerInfoPlayStatsPacket);
        connection.sendTCP(roomInformationPacket);
        connection.sendTCP(roomPlayerInformationPacket);

        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession != null) {
            this.gameSessionManager.removeGameSession(gameSession);
        }
    }

    public void handlePlayerPickingUpCrystal(Connection connection, Packet packet) {
        if (connection.getClient() == null) return;

        C2SMatchplayPlayerPicksUpCrystal playerPicksUpCrystalPacket = new C2SMatchplayPlayerPicksUpCrystal(packet);
        Room room = connection.getClient().getActiveRoom();
        if (room != null) { // shouldn't happen
            switch (room.getMode()) {
                case GameMode.GUARDIAN:
                    this.guardianModeHandler.handlePlayerPickingUpCrystal(connection, playerPicksUpCrystalPacket);
                    break;
                case GameMode.BATTLE:
                    this.battleModeHandler.handlePlayerPickingUpCrystal(connection, playerPicksUpCrystalPacket);
                    break;
            }
        }
    }

    public void handlePlayerUseSkill(Connection connection, Packet packet) {
        if (connection.getClient() == null) return;

        C2SMatchplayUsesSkill playerUseSkill = new C2SMatchplayUsesSkill(packet);
        Room room = connection.getClient().getActiveRoom();
        if (room != null) { // shouldn't happen
            switch (room.getMode()) {
                case GameMode.GUARDIAN:
                    this.guardianModeHandler.handleUseOfSkill(connection, playerUseSkill);
                    break;
                case GameMode.BATTLE:
                    this.battleModeHandler.handleUseOfSkill(connection, playerUseSkill);
                    break;
            }
        }
    }

    public void handleSkillHitsTarget(Connection connection, Packet packet) {
        if (connection.getClient() == null) return;

        C2SMatchplaySkillHitsTarget skillHitsTarget = new C2SMatchplaySkillHitsTarget(packet);
        Room room = connection.getClient().getActiveRoom();
        if (room != null) { // shouldn't happen
            switch (room.getMode()) {
                case GameMode.GUARDIAN:
                    this.guardianModeHandler.handleSkillHitsTarget(connection, skillHitsTarget);
                    break;
                case GameMode.BATTLE:
                    this.battleModeHandler.handleSkillHitsTarget(connection, skillHitsTarget);
                    break;
            }
        }
    }

    public void handleSwapQuickSlotItems(Connection connection, Packet packet) {
        if (connection.getClient() == null) return;

        C2SMatchplaySwapQuickSlotItems swapQuickSlotItems = new C2SMatchplaySwapQuickSlotItems(packet);
        Room room = connection.getClient().getActiveRoom();
        if (room != null) { // shouldn't happen
            switch (room.getMode()) {
                case GameMode.GUARDIAN:
                    this.guardianModeHandler.handleSwapQuickSlotItems(connection, swapQuickSlotItems);
                    break;
                case GameMode.BATTLE:
                    this.battleModeHandler.handleSwapQuickSlotItems(connection, swapQuickSlotItems);
                    break;
            }
        }
    }

    public void handleRankingPersonalDataReqPacket(Connection connection, Packet packet) {
        C2SRankingPersonalDataRequestPacket rankingPersonalDataRequestPacket = new C2SRankingPersonalDataRequestPacket(packet);
        byte gameMode = rankingPersonalDataRequestPacket.getGameMode();

        Player activePlayer = connection.getClient().getActivePlayer();
        if (activePlayer == null) {
            S2CRankingPersonalDataAnswerPacket rankingPersonalDataAnswerPacket = new S2CRankingPersonalDataAnswerPacket((char) 1, gameMode, new Player(), 0);
            connection.sendTCP(rankingPersonalDataAnswerPacket);
        } else {
            Player player = playerService.findByNameFetched(rankingPersonalDataRequestPacket.getNickname());
            if (player != null) {
                int ranking = playerService.getPlayerRankingByName(player.getName(), gameMode);

                S2CRankingPersonalDataAnswerPacket rankingPersonalDataAnswerPacket = new S2CRankingPersonalDataAnswerPacket((char) 0, gameMode, player, ranking);
                connection.sendTCP(rankingPersonalDataAnswerPacket);
            } else {
                S2CRankingPersonalDataAnswerPacket rankingPersonalDataAnswerPacket = new S2CRankingPersonalDataAnswerPacket((char) 1, gameMode, activePlayer, 0);
                connection.sendTCP(rankingPersonalDataAnswerPacket);
            }
        }
    }

    public void handleRankingDataReqPacket(Connection connection, Packet packet) {
        C2SRankingDataRequestPacket rankingDataRequestPacket = new C2SRankingDataRequestPacket(packet);
        int page = rankingDataRequestPacket.getPage();
        byte gameMode = rankingDataRequestPacket.getGameMode();

        String gameModeRP;
        if (gameMode == GameMode.BASIC)
            gameModeRP = "playerStatistic.basicRP";
        else if (gameMode == GameMode.BATTLE)
            gameModeRP = "playerStatistic.battleRP";
        else
            gameModeRP = "playerStatistic.guardianRP";
        List<Player> allPlayers = playerService.findAllByAlreadyCreatedPageable(PageRequest.of(page == 1 ? 0 : page - 1, 10,
                Sort.by(gameModeRP).descending().and(Sort.by("created"))));

        S2CRankingDataAnswerPacket rankingDataAnswerPacket = new S2CRankingDataAnswerPacket((char) 0, gameMode, page, allPlayers);
        connection.sendTCP(rankingDataAnswerPacket);
    }

    public void tryDetectSpeedHack(Connection connection) {
        if (connection == null || connection.getClient() == null) return;

        long time = System.currentTimeMillis();
        GameSession activeGameSession = connection.getClient().getActiveGameSession();
        Room room = connection.getClient().getActiveRoom();
        if (activeGameSession != null && activeGameSession.isSpeedHackCheckActive() && room != null && room.getStatus() == RoomStatus.Running) {
            long lastKeepAliveTime = connection.getClient().getLastHearBeatTime();
            long delta = time - lastKeepAliveTime;
            boolean maybeSpeedHack = lastKeepAliveTime > 0 && delta < 9500;
            if (maybeSpeedHack) {
                boolean wasFirstRecognitionIgnoredForCurrentClient = activeGameSession.getFirstSpeedHackRecognitionIgnoredForClients().stream()
                        .filter(c -> c == connection.getClient())
                        .findFirst()
                        .isPresent();

                // With this if we avoid a possible false negative
                if (wasFirstRecognitionIgnoredForCurrentClient) {
                    String message = "ARE YOU HACKING? PLEASE STOP OTHERWISE WE'LL PUNISH YOU!";
                    S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", message);
                    connection.sendTCP(chatRoomAnswerPacket);
                    log.warn(String.format("Player %s is maybe hacking", connection.getClient().getActivePlayer().getName()));
                } else {
                    activeGameSession.getFirstSpeedHackRecognitionIgnoredForClients().add(connection.getClient());
                }
            }
        }

        connection.getClient().setLastHearBeatTime(time);
    }

    public void handleHeartBeatPacket(Connection connection, Packet packet) {
        if (!GlobalSettings.IsAntiCheatEnabled) return;
        String hostAddress = connection.getClient().getIp();
        ClientWhitelist clientWhitelist = clientWhitelistService.findByIpAndHwid(hostAddress, connection.getHwid());
        if (clientWhitelist == null)
            handleDisconnected(connection);
    }

    public void handleUnknown(Connection connection, Packet packet) {
        Packet unknownAnswer = new Packet((char) (packet.getPacketId() + 1));
        if (unknownAnswer.getPacketId() == (char) 0x200E) {
            unknownAnswer.write((char) 1);
        }
        else {
            unknownAnswer.write((short) 0);
        }
        connection.sendTCP(unknownAnswer);
    }

    private GuildMember getGuildMemberByPlayerPositionInGuild(int playerPositionInGuild, GuildMember guildMember) {
        List<GuildMember> memberList = guildMember.getGuild()
                .getMemberList()
                .stream().filter(x -> !x.getWaitingForApproval())
                .collect(Collectors.toList());
        if (memberList.size() < playerPositionInGuild) {
            return null;
        }

        GuildMember dismissMember = memberList.get(playerPositionInGuild - 1);
        return dismissMember;
    }

    private void internalHandleRoomPositionChange(Connection connection, RoomPlayer roomPlayer, boolean freeOldPosition, short oldPosition, short newPosition) {
        Room room = connection.getClient().getActiveRoom();
        if (freeOldPosition) {
            room.getPositions().set(oldPosition, RoomPositionState.Free);
        }

        room.getPositions().set(newPosition, RoomPositionState.InUse);
        roomPlayer.setPosition(newPosition);
        S2CRoomPositionChangeAnswerPacket roomPositionChangePacket = new S2CRoomPositionChangeAnswerPacket((char) 0, oldPosition, newPosition);
        this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(roomPositionChangePacket));
    }

    private void internalHandleRoomCreate(Connection connection, Room room) {
        room.getPositions().set(0, RoomPositionState.InUse);
        room.setAllowBattlemon((byte) 0);

        byte players = room.getPlayers();
        if (players == 2) {
            room.getPositions().set(2, RoomPositionState.Locked);
            room.getPositions().set(3, RoomPositionState.Locked);
        }

        Player activePlayer = connection.getClient().getActivePlayer();

        RoomPlayer roomPlayer = new RoomPlayer();
        roomPlayer.setPlayer(activePlayer);
        roomPlayer.setGuildMember(guildMemberService.getByPlayer(activePlayer));
        roomPlayer.setClothEquipment(clothEquipmentService.findClothEquipmentById(roomPlayer.getPlayer().getClothEquipment().getId()));
        roomPlayer.setStatusPointsAddedDto(clothEquipmentService.getStatusPointsFromCloths(roomPlayer.getPlayer()));
        roomPlayer.setPosition((short) 0);
        roomPlayer.setMaster(true);
        roomPlayer.setFitting(false);
        room.getRoomPlayerList().add(roomPlayer);

        this.gameHandler.getRoomList().add(room);
        connection.getClient().setActiveRoom(room);
        connection.getClient().setInLobby(false);

        S2CRoomCreateAnswerPacket roomCreateAnswerPacket = new S2CRoomCreateAnswerPacket((char) 0, (byte) 0, (byte) 0, (byte) 0);
        connection.sendTCP(roomCreateAnswerPacket);

        S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
        connection.sendTCP(roomInformationPacket);

        S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(room.getRoomPlayerList());
        connection.sendTCP(roomPlayerInformationPacket);

        this.refreshLobbyRoomListForAllClients(connection);
        this.refreshLobbyPlayerListForAllClients();

        // TODO: Temporarily. Delete these lines if spectators work
        for (int i = 5; i < 9; i++) {
            connection.getClient().getActiveRoom().getPositions().set(i, RoomPositionState.Locked);
            S2CRoomSlotCloseAnswerPacket roomSlotCloseAnswerPacket = new S2CRoomSlotCloseAnswerPacket((byte) i, true);
            this.gameHandler.getClientsInRoom(connection.getClient().getActiveRoom().getRoomId()).forEach(c -> c.getConnection().sendTCP(roomSlotCloseAnswerPacket));
        }
    }

    private void refreshLobbyRoomListForAllClients(Connection connection) {
        long playerIdOfCurrentConnection = connection.getClient().getActivePlayer().getId();
        this.gameHandler.getClientsInLobby().forEach(c -> {
            if (c != null && c.getConnection() != null && c.getConnection().isConnected()) {
                S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(this.getFilteredRoomsForClient(c));
                boolean isNotActivePlayer = !c.getActivePlayer().getId().equals(playerIdOfCurrentConnection);
                if (isNotActivePlayer)
                    c.getConnection().sendTCP(roomListAnswerPacket);
            }
        });
    }

    private void refreshLobbyPlayerListForAllClients() {
        this.gameHandler.getClientsInLobby().forEach(c -> {
            if (c != null && c.getConnection() != null && c.getConnection().isConnected()) {
                byte currentPage = c.getLobbyCurrentPlayerListPage();
                List<Player> lobbyPlayerList = this.gameHandler.getPlayersInLobby().stream()
                        .skip(currentPage == 1 ? 0 : (currentPage * 10) - 10)
                        .limit(10)
                        .collect(Collectors.toList());
                S2CLobbyUserListAnswerPacket lobbyUserListAnswerPacket = new S2CLobbyUserListAnswerPacket(lobbyPlayerList);
                c.getConnection().sendTCP(lobbyUserListAnswerPacket);
            }
        });
    }

    private void handleRoomPlayerChanges(Connection connection) {
        Room room = connection.getClient().getActiveRoom();

        if (room != null) {
            List<RoomPlayer> roomPlayerList = room.getRoomPlayerList();
            Optional<RoomPlayer> roomPlayer = roomPlayerList.stream()
                    .filter(x -> x.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                    .findFirst();

            final short playerPosition = roomPlayer.isPresent() ? roomPlayer.get().getPosition() : -1;
            boolean isMaster = roomPlayer.isPresent() && roomPlayer.get().isMaster();

            if (isMaster) {
                roomPlayerList.stream()
                        .filter(rp -> !rp.isMaster())
                        .findFirst()
                        .ifPresent(rp -> {
                            rp.setMaster(true);
                            rp.setReady(false);
                        });
            }

            roomPlayerList.removeIf(rp -> rp.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()));

            this.gameHandler.getRoomList().stream()
                    .filter(r -> r.getRoomId() == room.getRoomId())
                    .findAny()
                    .ifPresent(r -> r.setRoomPlayerList(roomPlayerList));
            this.gameHandler.getRoomList().removeIf(r -> r.getRoomPlayerList().isEmpty());

            if (connection.getClient().getActiveGameSession() == null) {
                S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(roomPlayerList);
                this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> {
                    if (c != null) {
                        if (c.getActiveRoom() != null) {
                            c.getActiveRoom().setRoomPlayerList(roomPlayerList);
                            c.getActiveRoom().getPositions().set(playerPosition, RoomPositionState.Free);
                        }

                        if (!c.getActivePlayer().getId().equals(connection.getClient().getActivePlayer().getId()) && c.getConnection() != null && c.getConnection().isConnected())
                            c.getConnection().sendTCP(roomPlayerInformationPacket);
                    }
                });

                S2CRoomPositionChangeAnswerPacket roomPositionChangeAnswerPacket = new S2CRoomPositionChangeAnswerPacket((char) 0, playerPosition, (short) 9);
                this.gameHandler.getClientsInRoom(connection.getClient().getActiveRoom().getRoomId()).forEach(c -> {
                    if (c != null && !c.getActivePlayer().getId().equals(connection.getClient().getActivePlayer().getId()) && c.getConnection() != null && c.getConnection().isConnected())
                        c.getConnection().sendTCP(roomPositionChangeAnswerPacket);
                });
            } else {
                GameSession gameSession = this.gameSessionManager.getGameSessionBySessionId(connection.getClient().getActiveGameSession().getSessionId());
                if (gameSession != null) {
                    gameSession.getClients().removeIf(c -> c.getActivePlayer() != null && connection.getClient().getActivePlayer() != null
                                    && c.getActivePlayer().getId().equals(connection.getClient().getActivePlayer().getId()));
                }
                connection.getClient().setActiveGameSession(null);
                this.gameHandler.getClientList().stream()
                        .filter(c -> c.getActivePlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                        .findAny()
                        .ifPresent(c -> c.setActiveGameSession(null));
            }

            connection.getClient().setActiveRoom(null);
            this.gameHandler.getClientList().stream()
                    .filter(c -> c.getActivePlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                    .findAny()
                    .ifPresent(c -> c.setActiveRoom(null));

            S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(roomPlayerList);
            this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(roomPlayerInformationPacket));
            this.updateRoomForAllPlayersInMultiplayer(connection, room);
        }
    }

    private List<Room> getFilteredRoomsForClient(Client client) {
        int clientRoomModeFilter = client.getLobbyGameModeTabFilter();
        int currentRoomListPage = client.getLobbyCurrentRoomListPage() < 0 ? 0 : client.getLobbyCurrentRoomListPage();
        return this.gameHandler.getRoomList().stream()
                .filter(x -> clientRoomModeFilter == GameMode.ALL || getRoomMode(x) == clientRoomModeFilter)
                .skip(currentRoomListPage * 5)
                .limit(5)
                .collect(Collectors.toList());
    }

    private void updateRoomForAllPlayersInMultiplayer(Connection connection, Room room) {
        S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
        this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(roomInformationPacket));
        this.refreshLobbyRoomListForAllClients(connection);
    }

    private int getRoomMode(Room room) {
        if (room.getAllowBattlemon() == 2) {
            return GameMode.BATTLEMON;
        }

        return room.getMode();
    }

    private void sendPacketToAllInRoom(Connection connection, Packet packet) {
        this.gameHandler.getClientsInRoom(connection.getClient().getActiveRoom().getRoomId())
                .forEach(c -> c.getConnection().sendTCP(packet));
    }

    private short getRoomId() {
        List<Short> roomIds = this.gameHandler.getRoomList().stream().map(Room::getRoomId).collect(Collectors.toList());
        List<Short> sortedRoomIds = roomIds.stream().sorted().collect(Collectors.toList());
        short currentRoomId = 0;
        for (Short roomId : sortedRoomIds) {
            if (roomId != currentRoomId) {
                return currentRoomId;
            }

            currentRoomId++;
        }

        return currentRoomId;
    }

    private void handleRoomChat(Connection connection, Room room, C2SChatRoomReqPacket chatRoomReqPacket, S2CChatRoomAnswerPacket chatRoomAnswerPacket) {
        if (room == null) return;

        this.handleGuardianModeCommands(connection, room, chatRoomReqPacket);

        boolean isTeamChat = chatRoomReqPacket.getType() == 1;
        if (isTeamChat) {
            short senderPos = -1;
            for (RoomPlayer rp : room.getRoomPlayerList()) {
                if (connection.getClient().getActivePlayer().getId().equals(rp.getPlayer().getId())) {
                    senderPos = rp.getPosition();
                    break;
                }
            }

            if (senderPos < 0) return;
            for (Client c: this.gameHandler.getClientsInRoom(room.getRoomId())) {
                for (RoomPlayer rp : c.getActiveRoom().getRoomPlayerList()) {
                    if (c.getActivePlayer().getId().equals(rp.getPlayer().getId()) && areInSameTeam(senderPos, rp.getPosition())) {
                        c.getConnection().sendTCP(chatRoomAnswerPacket);
                    }
                }
            }
            connection.sendTCP(chatRoomAnswerPacket); // Send to sender
            return;
        }
        this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(chatRoomAnswerPacket));
    }

    private void handleGuardianModeCommands(Connection connection, Room room, C2SChatRoomReqPacket chatRoomReqPacket) {
        boolean isGuardian = getRoomMode(room) == GameMode.GUARDIAN;
        boolean isRoomMaster = room.getRoomPlayerList().stream()
                .filter(rp -> rp.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                .findAny()
                .get()
                .isMaster();
        String message = chatRoomReqPacket.getMessage();
        if (isRoomMaster && isGuardian && message.contains("-hard")) {
            if (!this.isAllowedToChangeMode(room)) return;
            room.setHardMode(!room.isHardMode());
            S2CChatRoomAnswerPacket hardModeChangedPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", String.format("Hard mode %s", room.isHardMode() ? "ON" : "OFF"));
            this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(hardModeChangedPacket));
        }

        if (isRoomMaster && isGuardian && message.contains("-arcade")) {
            if (!this.isAllowedToChangeMode(room)) return;
            room.setArcade(!room.isArcade());
            S2CChatRoomAnswerPacket arcadeChangedPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", String.format("Arcade is NOT implemented yet"));
            this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(arcadeChangedPacket));
        }

        if (isRoomMaster && isGuardian && message.contains("-random")) {
            if (!this.isAllowedToChangeMode(room)) return;
            room.setRandomGuardians(!room.isRandomGuardians());
            S2CChatRoomAnswerPacket randomGuardianChangedPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", String.format("Random mode %s", room.isRandomGuardians() ? "ON" : "OFF"));
            this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(randomGuardianChangedPacket));
        }
    }

    private boolean isAllowedToChangeMode(Room room) {
        List<RoomPlayer> activePlayingPlayers = room.getRoomPlayerList().stream().filter(x -> x.getPosition() < 4).collect(Collectors.toList());
        boolean allPlayerAreSixty = activePlayingPlayers.stream().allMatch(x -> x.getPlayer().getLevel() == 60);
        if (!allPlayerAreSixty) {
            S2CChatRoomAnswerPacket hardModeChangedPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", "All in the room must be lvl 60 to be able to change modes");
            this.gameHandler.getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(hardModeChangedPacket));
            return false;
        }

        return true;
    }

    private boolean areInSameTeam(int playerPos1, int playerPos2) {
        boolean bothInRedTeam = playerPos1 == 0 && playerPos2 == 2 || playerPos1 == 2 && playerPos2 == 0;
        boolean bothInBlueTeam = playerPos1 == 1 && playerPos2 == 3 || playerPos1 == 3 && playerPos2 == 1;
        return bothInRedTeam || bothInBlueTeam;
    }
}
