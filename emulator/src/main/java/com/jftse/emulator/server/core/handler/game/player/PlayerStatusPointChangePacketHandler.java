package com.jftse.emulator.server.core.handler.game.player;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.player.C2SPlayerStatusPointChangePacket;
import com.jftse.emulator.server.core.packet.packets.player.S2CPlayerStatusPointChangePacket;
import com.jftse.emulator.server.core.service.ClothEquipmentService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.networking.packet.Packet;

public class PlayerStatusPointChangePacketHandler extends AbstractHandler {
    private C2SPlayerStatusPointChangePacket playerStatusPointChangePacket;

    private final PlayerService playerService;
    private final ClothEquipmentService clothEquipmentService;

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
        Player player = connection.getClient().getPlayer();

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

                connection.getClient().savePlayer(player);

                StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);

                S2CPlayerStatusPointChangePacket playerStatusPointChangeAnswerPacket = new S2CPlayerStatusPointChangePacket(player, statusPointsAddedDto);
                connection.sendTCP(playerStatusPointChangeAnswerPacket);
            }
        }
    }
}
