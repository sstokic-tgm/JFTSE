package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.client.EquippedPetSlots;
import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.client.GuildView;
import com.jftse.emulator.server.core.client.PlayerStatisticView;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.home.S2CHomeDataPacket;
import com.jftse.emulator.server.core.packets.inventory.*;
import com.jftse.emulator.server.core.packets.pet.S2CPetDataAnswerPacket;
import com.jftse.emulator.server.core.packets.player.*;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.pet.Pet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.PlayerLoadType;
import com.jftse.server.core.shared.packets.game.CMSGReceiveData;
import com.jftse.server.core.shared.packets.game.SMSGReceiveData;
import com.jftse.server.core.shared.packets.player.SMSGSetCouplePoints;
import com.jftse.server.core.shared.packets.shop.SMSGSetMoney;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
@PacketId(CMSGReceiveData.PACKET_ID)
public class GameServerDataRequestPacketHandler implements PacketHandler<FTConnection, CMSGReceiveData> {
    private final HomeService homeService;
    private final PetService petService;
    private final GuildMemberService guildMemberService;
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;
    private final PlayerStatisticService playerStatisticService;
    private final PlayerService playerService;

    public GameServerDataRequestPacketHandler() {
        homeService = ServiceManager.getInstance().getHomeService();
        petService = ServiceManager.getInstance().getPetService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        pocketService = ServiceManager.getInstance().getPocketService();
        playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public void handle(FTConnection connection, CMSGReceiveData packet) {
        FTClient client = connection.getClient();
        FTPlayer player = client.getPlayer();

        byte requestType = packet.getDataType();

        /*while (client.getCurrentRequestType().get() != requestType) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }*/

        if (!client.updateDataRequestStep(requestType)) {
            return;
        }

        //ThreadManager.getInstance().newTask(() -> {
        if (requestType == 0) {
            Pocket pocket = pocketService.findById(player.getPocketId());
            PlayerStatistic playerStatistic = playerStatisticService.findPlayerStatisticById(player.getPlayerStatisticId());
            GuildMember guildMember = guildMemberService.getByPlayer(player.getId());

            Player dbPlayer = playerService.findWithEquipmentById(player.getId());
            player = client.loadPlayer(dbPlayer, PlayerLoadType.FULL_EQUIPMENT);
            boolean refreshed = client.refreshPlayer(player);
            if (!refreshed) {
                log.error("Failed to refresh player data for {}", player.getName());
                SMSGReceiveData response = SMSGReceiveData.builder()
                        .dataType(requestType)
                        .unk0((byte) 1)
                        .build();
                connection.sendTCP(response);
                return;
            }

            if (guildMember != null && !guildMember.getWaitingForApproval()) {
                Guild guild = guildMember.getGuild();
                player.setGuildMemberId(guildMember.getId());
                player.setGuild(GuildView.fromEntity(guild));
            }

            S2CUnknownPlayerInfoDataPacket unknownPlayerInfoDataPacket = new S2CUnknownPlayerInfoDataPacket(player, pocket, playerStatistic);
            connection.sendTCP(unknownPlayerInfoDataPacket);

            S2CPlayerLevelExpPacket playerLevelExpPacket = new S2CPlayerLevelExpPacket((byte) player.getLevel(), player.getExpPoints());
            connection.sendTCP(playerLevelExpPacket);
        } else if (requestType == 1) {
            Pocket pocket = pocketService.findById(player.getPocketId());
            List<PlayerPocket> playerPocketList = playerPocketService.getPlayerPocketItems(pocket);
            S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(playerPocketList);
            connection.sendTCP(inventoryDataPacket);
        } else if (requestType == 2) {
            AccountHome accountHome = homeService.findAccountHomeByAccountId(client.getAccountId());
            S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
            connection.sendTCP(homeDataPacket);

            List<Pet> petList = petService.findAllByPlayerId(player.getId());
            S2CPetDataAnswerPacket petDataAnswerPacket = new S2CPetDataAnswerPacket(petList);
            connection.sendTCP(petDataAnswerPacket);
        } else if (requestType == 3) {
            PlayerStatistic playerStatistic = playerStatisticService.findPlayerStatisticById(player.getPlayerStatisticId());
            player.setPlayerStatistic(PlayerStatisticView.fromEntity(playerStatistic));
            player.setPetSlots(EquippedPetSlots.defaultSlots());

            S2CPlayerInfoPlayStatsPacket playerInfoPlayStatsPacket = new S2CPlayerInfoPlayStatsPacket(playerStatistic);
            S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, player);
            S2CInventoryWearQuickAnswerPacket inventoryWearQuickAnswerPacket = new S2CInventoryWearQuickAnswerPacket(player.getQuickSlots().toList());
            S2CInventoryWearToolAnswerPacket inventoryWearToolAnswerPacket = new S2CInventoryWearToolAnswerPacket(player.getToolSlots().toList());
            S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(player.getSpecialSlots().toList());
            S2CInventoryWearCardAnswerPacket inventoryWearCardAnswerPacket = new S2CInventoryWearCardAnswerPacket(player.getCardSlots().toList());
            S2CInventoryWearBattlemonAnswerPacket inventoryWearBattlemonAnswerPacket = new S2CInventoryWearBattlemonAnswerPacket(player.getPetSlots().toList());

            connection.sendTCP(inventoryWearQuickAnswerPacket);
            connection.sendTCP(inventoryWearToolAnswerPacket);
            connection.sendTCP(inventoryWearSpecialAnswerPacket);
            connection.sendTCP(inventoryWearCardAnswerPacket);
            connection.sendTCP(inventoryWearBattlemonAnswerPacket);
            connection.sendTCP(inventoryWearClothAnswerPacket);
            connection.sendTCP(playerInfoPlayStatsPacket);
        } else if (requestType == 4) {
            SMSGSetMoney moneyPacket = SMSGSetMoney.builder()
                    .ap(client.getAp().get())
                    .gold(player.getGold())
                    .build();
            connection.sendTCP(moneyPacket);

            SMSGSetCouplePoints couplePointsPacket = SMSGSetCouplePoints.builder().amount(player.getCouplePoints()).build();
            connection.sendTCP(couplePointsPacket);
        }
        //});
    }
}
