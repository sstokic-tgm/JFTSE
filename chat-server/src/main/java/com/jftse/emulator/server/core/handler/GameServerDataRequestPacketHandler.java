package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.common.utilities.StreamUtils;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.gameserver.C2SGameServerRequestPacket;
import com.jftse.emulator.server.core.packets.gameserver.S2CGameServerAnswerPacket;
import com.jftse.emulator.server.core.packets.home.S2CHomeDataPacket;
import com.jftse.emulator.server.core.packets.inventory.*;
import com.jftse.emulator.server.core.packets.messenger.S2CClubMembersListAnswerPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CFriendRequestNotificationPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CFriendsListAnswerPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CRelationshipAnswerPacket;
import com.jftse.emulator.server.core.packets.pet.S2CPetDataAnswerPacket;
import com.jftse.emulator.server.core.packets.player.*;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.core.service.impl.ClothEquipmentServiceImpl;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.pet.Pet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Log4j2
@PacketOperationIdentifier(PacketOperations.C2SGameReceiveData)
public class GameServerDataRequestPacketHandler extends AbstractPacketHandler {
    private C2SGameServerRequestPacket gameServerRequestPacket;

    private final PlayerService playerService;
    private final HomeService homeService;
    private final PetService petService;
    private final GuildMemberService guildMemberService;
    private final PlayerPocketService playerPocketService;
    private final ClothEquipmentServiceImpl clothEquipmentService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;
    private final ToolSlotEquipmentService toolSlotEquipmentService;
    private final SpecialSlotEquipmentService specialSlotEquipmentService;
    private final CardSlotEquipmentService cardSlotEquipmentService;
    private final BattlemonSlotEquipmentService battlemonSlotEquipmentService;
    private final SocialService socialService;
    private final PocketService pocketService;
    private final PlayerStatisticService playerStatisticService;

    private final RProducerService rProducerService;

    public GameServerDataRequestPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        homeService = ServiceManager.getInstance().getHomeService();
        petService = ServiceManager.getInstance().getPetService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        quickSlotEquipmentService = ServiceManager.getInstance().getQuickSlotEquipmentService();
        toolSlotEquipmentService = ServiceManager.getInstance().getToolSlotEquipmentService();
        specialSlotEquipmentService = ServiceManager.getInstance().getSpecialSlotEquipmentService();
        cardSlotEquipmentService = ServiceManager.getInstance().getCardSlotEquipmentService();
        battlemonSlotEquipmentService = ServiceManager.getInstance().getBattlemonSlotEquipmentService();
        socialService = ServiceManager.getInstance().getSocialService();
        pocketService = ServiceManager.getInstance().getPocketService();
        playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public boolean process(Packet packet) {
        gameServerRequestPacket = new C2SGameServerRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        Player player = client.getPlayer();
        Account account = client.getAccount();

        byte requestType = gameServerRequestPacket.getRequestType();

        while (client.getCurrentRequestType().get() != requestType) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }

        // init data request packets and pass level & exp and home/house data
        if (requestType == 0) {
            // reset pocket
            List<Player> playerList = playerService.findAllByAccount(account);
            for (Player p : playerList) {
                List<PlayerPocket> playerPocketList = playerPocketService.getPlayerPocketItems(p.getPocket());
                StreamUtils.batches(playerPocketList, 20).forEach(pocketList -> {
                    List<Packet> inventoryItemRemoveAnswerPackets = new ArrayList<>();
                    pocketList.forEach(pocket -> inventoryItemRemoveAnswerPackets.add(new S2CInventoryItemRemoveAnswerPacket((int) pocket.getId().longValue())));
                    connection.sendTCP(inventoryItemRemoveAnswerPackets.toArray(new Packet[0]));
                });
            }

            StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
            Pocket pocket = pocketService.findById(player.getPocket().getId());
            PlayerStatistic playerStatistic = playerStatisticService.findPlayerStatisticById(player.getPlayerStatistic().getId());

            S2CUnknownPlayerInfoDataPacket unknownPlayerInfoDataPacket = new S2CUnknownPlayerInfoDataPacket(player, pocket, statusPointsAddedDto, playerStatistic);
            S2CPlayerLevelExpPacket playerLevelExpPacket = new S2CPlayerLevelExpPacket(player.getLevel(), player.getExpPoints());
            S2CCouplePointsDataPacket couplePointsDataPacket = new S2CCouplePointsDataPacket(player.getCouplePoints());
            connection.sendTCP(unknownPlayerInfoDataPacket);
            connection.sendTCP(playerLevelExpPacket);
            connection.sendTCP(couplePointsDataPacket);

            player.setOnline(true);
            client.savePlayer(player);

            AccountHome accountHome = homeService.findAccountHomeByAccountId(account.getId());

            S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
            connection.sendTCP(homeDataPacket);

            List<Pet> petList = petService.findAllByPlayerId(player.getId());

            S2CPetDataAnswerPacket petDataAnswerPacket = new S2CPetDataAnswerPacket(petList);
            connection.sendTCP(petDataAnswerPacket);

            S2CGameServerAnswerPacket gameServerAnswerPacket = new S2CGameServerAnswerPacket(requestType, (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);

            client.getCurrentRequestType().incrementAndGet();
        } else if (requestType == 1) {
            List<Friend> friends = socialService.getFriendList(player, EFriendshipState.Friends);
            S2CFriendsListAnswerPacket s2CFriendsListAnswerPacket = new S2CFriendsListAnswerPacket(friends);
            connection.sendTCP(s2CFriendsListAnswerPacket);

            // update friend list for other online friends
            friends.stream()
                    .filter(f -> f.getFriend().getOnline())
                    .forEach(f -> {
                        List<Friend> onlineFriends = socialService.getFriendList(f.getFriend(), EFriendshipState.Friends);
                        S2CFriendsListAnswerPacket friendListAnswerPacket = new S2CFriendsListAnswerPacket(onlineFriends);
                        FTConnection friendConnection = GameManager.getInstance().getConnectionByPlayerId(f.getFriend().getId());
                        if (friendConnection != null) {
                            friendConnection.sendTCP(friendListAnswerPacket);
                        } else {
                            rProducerService.send("playerId", f.getFriend().getId(), friendListAnswerPacket);
                        }
                    });

            List<Friend> friendsWaitingForApproval = socialService.getFriendListByFriend(player, EFriendshipState.WaitingApproval);
            friendsWaitingForApproval.forEach(f -> {
                S2CFriendRequestNotificationPacket s2CFriendRequestNotificationPacket = new S2CFriendRequestNotificationPacket(f.getPlayer().getName());
                connection.sendTCP(s2CFriendRequestNotificationPacket);
            });

            Friend myRelation = socialService.getRelationship(player);
            if (myRelation != null) {
                S2CRelationshipAnswerPacket s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(myRelation);
                connection.sendTCP(s2CRelationshipAnswerPacket);

                FTConnection friendRelationClient = GameManager.getInstance().getConnectionByPlayerId(myRelation.getFriend().getId());
                Friend friendRelation = socialService.getRelationship(myRelation.getFriend());
                if (friendRelationClient != null && friendRelation != null) {
                    s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(friendRelation);
                    friendRelationClient.sendTCP(s2CRelationshipAnswerPacket);
                } else if (friendRelation != null) {
                    s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(friendRelation);
                    rProducerService.send("playerId", friendRelation.getPlayer().getId(), s2CRelationshipAnswerPacket);
                }
            }

            GuildMember guildMember = guildMemberService.getByPlayer(player);
            if (guildMember != null && guildMember.getGuild() != null) {
                guildMember.getGuild().getMemberList().stream()
                        .filter(gm -> !gm.getPlayer().getId().equals(guildMember.getPlayer().getId()) && !gm.getWaitingForApproval())
                        .forEach(x -> {
                            List<GuildMember> guildMembers = socialService.getGuildMemberList(x.getPlayer());

                            S2CClubMembersListAnswerPacket s2CClubMembersListAnswerPacket = new S2CClubMembersListAnswerPacket(guildMembers);
                            FTConnection guildMemberConnection = GameManager.getInstance().getConnectionByPlayerId(x.getPlayer().getId());
                            if (guildMemberConnection != null) {
                                guildMemberConnection.sendTCP(s2CClubMembersListAnswerPacket);
                            } else {
                                rProducerService.send("playerId", x.getPlayer().getId(), s2CClubMembersListAnswerPacket);
                            }
                        });
            }

            S2CGameServerAnswerPacket gameServerAnswerPacket = new S2CGameServerAnswerPacket(requestType, (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);

            client.getCurrentRequestType().incrementAndGet();
        }
        // pass inventory & equipped items
        else if (requestType == 2) {
            StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
            Map<String, Integer> equippedCloths = clothEquipmentService.getEquippedCloths(player);
            List<Integer> equippedQuickSlots = quickSlotEquipmentService.getEquippedQuickSlots(player);
            List<Integer> equippedToolSlots = toolSlotEquipmentService.getEquippedToolSlots(player);
            List<Integer> equippedSpecialSlots = specialSlotEquipmentService.getEquippedSpecialSlots(player);
            List<Integer> equippedCardSlots = cardSlotEquipmentService.getEquippedCardSlots(player);
            List<Integer> equippedBattlemonSlots = battlemonSlotEquipmentService.getEquippedBattlemonSlots(player);

            List<PlayerPocket> playerPocketList = playerPocketService.getPlayerPocketItems(player.getPocket());
            StreamUtils.batches(playerPocketList, 10).forEach(pocketList -> {
                S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(pocketList);
                connection.sendTCP(inventoryDataPacket);
            });

            S2CPlayerStatusPointChangePacket playerStatusPointChangePacket = new S2CPlayerStatusPointChangePacket(player, statusPointsAddedDto);
            S2CPlayerInfoPlayStatsPacket playerInfoPlayStatsPacket = new S2CPlayerInfoPlayStatsPacket(player.getPlayerStatistic());
            S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, equippedCloths, player, statusPointsAddedDto);
            S2CInventoryWearQuickAnswerPacket inventoryWearQuickAnswerPacket = new S2CInventoryWearQuickAnswerPacket(equippedQuickSlots);
            S2CInventoryWearToolAnswerPacket inventoryWearToolAnswerPacket = new S2CInventoryWearToolAnswerPacket(equippedToolSlots);
            S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(equippedSpecialSlots);
            S2CInventoryWearCardAnswerPacket inventoryWearCardAnswerPacket = new S2CInventoryWearCardAnswerPacket(equippedCardSlots);
            S2CInventoryWearBattlemonAnswerPacket inventoryWearBattlemonAnswerPacket = new S2CInventoryWearBattlemonAnswerPacket(equippedBattlemonSlots);

            connection.sendTCP(playerStatusPointChangePacket);
            connection.sendTCP(playerInfoPlayStatsPacket);
            connection.sendTCP(inventoryWearClothAnswerPacket);
            connection.sendTCP(inventoryWearQuickAnswerPacket);
            connection.sendTCP(inventoryWearToolAnswerPacket);
            connection.sendTCP(inventoryWearSpecialAnswerPacket);
            connection.sendTCP(inventoryWearCardAnswerPacket);
            connection.sendTCP(inventoryWearBattlemonAnswerPacket);

            S2CGameServerAnswerPacket gameServerAnswerPacket = new S2CGameServerAnswerPacket(requestType, (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);

            client.getCurrentRequestType().incrementAndGet();
        } else {
            S2CGameServerAnswerPacket gameServerAnswerPacket = new S2CGameServerAnswerPacket(requestType, (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);

            client.getCurrentRequestType().incrementAndGet();
        }
    }
}
