package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CClearInventoryPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.S2CDisconnectAnswerPacket;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SDisconnectRequest)
public class DisconnectPacketHandler extends AbstractPacketHandler {
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;

    public DisconnectPacketHandler() {
        this.pocketService = ServiceManager.getInstance().getPocketService();
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        S2CDisconnectAnswerPacket disconnectAnswerPacket = new S2CDisconnectAnswerPacket();
        connection.sendTCP(disconnectAnswerPacket);

        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient != null) {
            Player player = ftClient.getPlayer();
            GameManager.getInstance().handleRoomPlayerChanges((FTConnection) connection, true);

            Pocket pocket = pocketService.findById(player.getPocket().getId());
            List<PlayerPocket> playerPocketList = playerPocketService.getPlayerPocketItems(pocket);

            S2CClearInventoryPacket clearInventoryPacket = new S2CClearInventoryPacket(playerPocketList);
            connection.sendTCP(clearInventoryPacket);
        }

        connection.close();
    }
}
