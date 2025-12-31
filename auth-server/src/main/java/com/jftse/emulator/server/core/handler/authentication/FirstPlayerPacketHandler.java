package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.player.*;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.auth.CMSGLoginFirstPlayer;
import com.jftse.server.core.shared.packets.auth.SMSGLoginFirstPlayer;
import com.jftse.server.core.thread.ThreadManager;

import java.util.List;

@PacketId(CMSGLoginFirstPlayer.PACKET_ID)
public class FirstPlayerPacketHandler implements PacketHandler<FTConnection, CMSGLoginFirstPlayer> {
    private final ClothEquipmentService clothEquipmentService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;
    private final SpecialSlotEquipmentService specialSlotEquipmentService;
    private final ToolSlotEquipmentService toolSlotEquipmentService;
    private final CardSlotEquipmentService cardSlotEquipmentService;
    private final BattlemonSlotEquipmentService battlemonSlotEquipmentService;
    private final PocketService pocketService;
    private final PlayerStatisticService playerStatisticService;
    private final PlayerService playerService;
    private final HomeService homeService;

    public FirstPlayerPacketHandler() {
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        quickSlotEquipmentService = ServiceManager.getInstance().getQuickSlotEquipmentService();
        specialSlotEquipmentService = ServiceManager.getInstance().getSpecialSlotEquipmentService();
        toolSlotEquipmentService = ServiceManager.getInstance().getToolSlotEquipmentService();
        cardSlotEquipmentService = ServiceManager.getInstance().getCardSlotEquipmentService();
        battlemonSlotEquipmentService = ServiceManager.getInstance().getBattlemonSlotEquipmentService();
        pocketService = ServiceManager.getInstance().getPocketService();
        playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
        playerService = ServiceManager.getInstance().getPlayerService();
        homeService = ServiceManager.getInstance().getHomeService();
    }

    @Override
    public void handle(FTConnection connection, CMSGLoginFirstPlayer firstPlayerPacket) {
        FTClient client = connection.getClient();
        List<Player> playerList = playerService.findAllByAccount(client.getAccount());

        if (playerList.isEmpty()) {
            AccountHome accountHome = new AccountHome();
            accountHome.setAccount(client.getAccount());
            homeService.save(accountHome);

            Player player = new Player();
            player.setAccount(client.getAccount());
            player.setPlayerType(firstPlayerPacket.getPlayerType());
            player.setFirstPlayer(true);

            ClothEquipment clothEquipment = new ClothEquipment();
            clothEquipment = clothEquipmentService.save(clothEquipment);
            player.setClothEquipment(clothEquipment);

            QuickSlotEquipment quickSlotEquipment = new QuickSlotEquipment();
            quickSlotEquipment = quickSlotEquipmentService.save(quickSlotEquipment);
            player.setQuickSlotEquipment(quickSlotEquipment);

            SpecialSlotEquipment specialSlotEquipment = new SpecialSlotEquipment();
            specialSlotEquipment = specialSlotEquipmentService.save(specialSlotEquipment);
            player.setSpecialSlotEquipment(specialSlotEquipment);

            ToolSlotEquipment toolSlotEquipment = new ToolSlotEquipment();
            toolSlotEquipment = toolSlotEquipmentService.save(toolSlotEquipment);
            player.setToolSlotEquipment(toolSlotEquipment);

            CardSlotEquipment cardSlotEquipment = new CardSlotEquipment();
            cardSlotEquipment = cardSlotEquipmentService.save(cardSlotEquipment);
            player.setCardSlotEquipment(cardSlotEquipment);

                /*BattlemonSlotEquipment battlemonSlotEquipment = new BattlemonSlotEquipment();
                battlemonSlotEquipment = battlemonSlotEquipmentService.save(battlemonSlotEquipment);
                player.setBattlemonSlotEquipment(battlemonSlotEquipment);
                */

            Pocket pocket = new Pocket();
            pocket = pocketService.save(pocket);
            player.setPocket(pocket);

            PlayerStatistic playerStatistic = new PlayerStatistic();
            playerStatistic = playerStatisticService.save(playerStatistic);
            player.setPlayerStatistic(playerStatistic);

            player = playerService.save(player);

            SMSGLoginFirstPlayer response = SMSGLoginFirstPlayer.builder()
                    .result((char) 0)
                    .playerId(Math.toIntExact(player.getId()))
                    .playerType(player.getPlayerType())
                    .build();
            connection.sendTCP(response);
        } else {
            SMSGLoginFirstPlayer response = SMSGLoginFirstPlayer.builder()
                    .result((char) -1)
                    .playerId(0)
                    .playerType(firstPlayerPacket.getPlayerType())
                    .build();
            connection.sendTCP(response);
        }
    }
}
