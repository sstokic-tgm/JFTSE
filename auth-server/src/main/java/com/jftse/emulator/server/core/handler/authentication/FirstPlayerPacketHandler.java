package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.player.C2SFirstPlayerPacket;
import com.jftse.emulator.server.core.packets.player.S2CFirstPlayerAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.player.*;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.*;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SLoginFirstPlayerRequest)
public class FirstPlayerPacketHandler extends AbstractPacketHandler {
    private C2SFirstPlayerPacket firstPlayerPacket;

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
    public boolean process(Packet packet) {
        firstPlayerPacket = new C2SFirstPlayerPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        List<Player> playerList = playerService.findAllByAccount(client.getAccount());

        if (playerList.isEmpty()) {
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

            AccountHome accountHome = new AccountHome();
            accountHome.setAccount(client.getAccount());
            accountHome = homeService.save(accountHome);

            S2CFirstPlayerAnswerPacket firstPlayerAnswerPacket = new S2CFirstPlayerAnswerPacket((char) 0, player.getId(), player.getPlayerType());
            connection.sendTCP(firstPlayerAnswerPacket);
        } else {
            S2CFirstPlayerAnswerPacket firstPlayerAnswerPacket = new S2CFirstPlayerAnswerPacket((char) -1, 0L, (byte) 0);
            connection.sendTCP(firstPlayerAnswerPacket);
        }
    }
}
