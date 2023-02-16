package com.jftse.emulator.server.core.handler.player;

import com.jftse.emulator.server.core.packets.player.S2CPlayerStatusPointChangePacket;
import com.jftse.emulator.server.core.service.impl.ClothEquipmentServiceImpl;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.shared.packets.player.C2SPlayerStatusPointChangePacket;

@PacketOperationIdentifier(PacketOperations.C2SPlayerStatusPointChange)
public class PlayerStatusPointChangePacketHandler extends AbstractPacketHandler {
    private C2SPlayerStatusPointChangePacket playerStatusPointChangePacket;

    private final PlayerService playerService;
    private final ClothEquipmentServiceImpl clothEquipmentService;

    public PlayerStatusPointChangePacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
    }

    @Override
    public boolean process(Packet packet) {
        playerStatusPointChangePacket = new C2SPlayerStatusPointChangePacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        Player player = ftClient.getPlayer();

        // we can't change; attributes should be server sided
        if (player.getStatusPoints() == 0) {
            S2CPlayerStatusPointChangePacket playerStatusPointChangeAnswerPacket = new S2CPlayerStatusPointChangePacket(player, new StatusPointsAddedDto());
            connection.sendTCP(playerStatusPointChangeAnswerPacket);
        } else if (player.getStatusPoints() > 0 && playerStatusPointChangePacket.getStatusPoints() >= 0) {
            if (playerService.isStatusPointHack(playerStatusPointChangePacket, player)) {
                S2CPlayerStatusPointChangePacket playerStatusPointChangeAnswerPacket = new S2CPlayerStatusPointChangePacket(player, new StatusPointsAddedDto());
                connection.sendTCP(playerStatusPointChangeAnswerPacket);
            } else {
                player.setStrength(playerStatusPointChangePacket.getStrength());
                player.setStamina(playerStatusPointChangePacket.getStamina());
                player.setDexterity(playerStatusPointChangePacket.getDexterity());
                player.setWillpower(playerStatusPointChangePacket.getWillpower());
                player.setStatusPoints(playerStatusPointChangePacket.getStatusPoints());

                ftClient.savePlayer(player);

                StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);

                S2CPlayerStatusPointChangePacket playerStatusPointChangeAnswerPacket = new S2CPlayerStatusPointChangePacket(player, statusPointsAddedDto);
                connection.sendTCP(playerStatusPointChangeAnswerPacket);
            }
        }
    }
}
