package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.gameserver.C2SGameServerRequestPacket;
import com.jftse.emulator.server.core.packets.gameserver.S2CGameServerAnswerPacket;
import com.jftse.emulator.server.core.packets.home.S2CHomeDataPacket;
import com.jftse.emulator.server.core.packets.inventory.*;
import com.jftse.emulator.server.core.packets.pet.S2CPetDataAnswerPacket;
import com.jftse.emulator.server.core.packets.player.*;
import com.jftse.emulator.server.core.service.impl.ClothEquipmentServiceImpl;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.home.AccountHome;
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

import java.util.List;
import java.util.Map;

@PacketOperationIdentifier(PacketOperations.C2SGameReceiveData)
public class GameServerDataRequestPacketHandler extends AbstractPacketHandler {
    private C2SGameServerRequestPacket gameServerRequestPacket;

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
    private final PocketService pocketService;
    private final PlayerStatisticService playerStatisticService;

    public GameServerDataRequestPacketHandler() {
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
        pocketService = ServiceManager.getInstance().getPocketService();
        playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
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

        if (requestType == 0) {
            Pocket pocket = pocketService.findById(player.getPocket().getId());
            List<PlayerPocket> playerPocketList = playerPocketService.getPlayerPocketItems(pocket);

            S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(playerPocketList);
            connection.sendTCP(inventoryDataPacket);

            AccountHome accountHome = homeService.findAccountHomeByAccountId(account.getId());

            S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
            connection.sendTCP(homeDataPacket);

            S2CGameServerAnswerPacket gameServerAnswerPacket = new S2CGameServerAnswerPacket(requestType, (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);

            client.getCurrentRequestType().incrementAndGet();
        } else if (requestType == 1) {
            List<Pet> petList = petService.findAllByPlayerId(player.getId());

            S2CPetDataAnswerPacket petDataAnswerPacket = new S2CPetDataAnswerPacket(petList);
            connection.sendTCP(petDataAnswerPacket);

            S2CGameServerAnswerPacket gameServerAnswerPacket = new S2CGameServerAnswerPacket(requestType, (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);

            client.getCurrentRequestType().incrementAndGet();
        } else if (requestType == 2) {
            S2CPlayerLevelExpPacket playerLevelExpPacket = new S2CPlayerLevelExpPacket(player.getLevel(), player.getExpPoints());
            S2CCouplePointsDataPacket couplePointsDataPacket = new S2CCouplePointsDataPacket(player.getCouplePoints());

            connection.sendTCP(playerLevelExpPacket);
            connection.sendTCP(couplePointsDataPacket);

            Pocket pocket = pocketService.findById(player.getPocket().getId());
            PlayerStatistic playerStatistic = playerStatisticService.findPlayerStatisticById(player.getPlayerStatistic().getId());

            GuildMember guildMember = guildMemberService.getByPlayer(player);
            Guild guild = null;
            if (guildMember != null && !guildMember.getWaitingForApproval() && guildMember.getGuild() != null)
                guild = guildMember.getGuild();

            StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
            Map<String, Integer> equippedCloths = clothEquipmentService.getEquippedCloths(player);
            List<Integer> equippedQuickSlots = quickSlotEquipmentService.getEquippedQuickSlots(player);
            List<Integer> equippedToolSlots = toolSlotEquipmentService.getEquippedToolSlots(player);
            List<Integer> equippedSpecialSlots = specialSlotEquipmentService.getEquippedSpecialSlots(player);
            List<Integer> equippedCardSlots = cardSlotEquipmentService.getEquippedCardSlots(player);
            List<Integer> equippedBattlemonSlots = battlemonSlotEquipmentService.getEquippedBattlemonSlots(player);

            S2CPlayerInfoPlayStatsPacket playerInfoPlayStatsPacket = new S2CPlayerInfoPlayStatsPacket(player.getPlayerStatistic());
            S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, equippedCloths, player, statusPointsAddedDto);
            S2CInventoryWearQuickAnswerPacket inventoryWearQuickAnswerPacket = new S2CInventoryWearQuickAnswerPacket(equippedQuickSlots);
            S2CInventoryWearToolAnswerPacket inventoryWearToolAnswerPacket = new S2CInventoryWearToolAnswerPacket(equippedToolSlots);
            S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(equippedSpecialSlots);
            S2CInventoryWearCardAnswerPacket inventoryWearCardAnswerPacket = new S2CInventoryWearCardAnswerPacket(equippedCardSlots);
            S2CInventoryWearBattlemonAnswerPacket inventoryWearBattlemonAnswerPacket = new S2CInventoryWearBattlemonAnswerPacket(equippedBattlemonSlots);
            S2CUnknownPlayerInfoDataPacket unknownPlayerInfoDataPacket = new S2CUnknownPlayerInfoDataPacket(player, pocket, equippedCloths, statusPointsAddedDto, playerStatistic, guild);

            connection.sendTCP(inventoryWearQuickAnswerPacket);
            connection.sendTCP(inventoryWearToolAnswerPacket);
            connection.sendTCP(inventoryWearSpecialAnswerPacket);
            connection.sendTCP(inventoryWearCardAnswerPacket);
            connection.sendTCP(inventoryWearBattlemonAnswerPacket);
            connection.sendTCP(inventoryWearClothAnswerPacket);
            connection.sendTCP(unknownPlayerInfoDataPacket);
            connection.sendTCP(playerInfoPlayStatsPacket);

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
