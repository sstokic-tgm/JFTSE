package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.common.utilities.StreamUtils;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.packets.S2CDisconnectAnswerPacket;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;

import java.util.ArrayList;
import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SDisconnectRequest)
public class DisconnectPacketHandler extends AbstractPacketHandler {
    private final PlayerPocketService playerPocketService;

    public DisconnectPacketHandler() {
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = connection.getClient();
        if (ftClient != null) {
            Player player = ftClient.getPlayer();
            if (player != null) {
                // reset pocket
                List<PlayerPocket> playerPocketList = playerPocketService.getPlayerPocketItems(player.getPocket());
                StreamUtils.batches(playerPocketList, 20).forEach(pocketList -> {
                    List<Packet> inventoryItemRemoveAnswerPackets = new ArrayList<>();
                    pocketList.forEach(p -> inventoryItemRemoveAnswerPackets.add(new S2CInventoryItemRemoveAnswerPacket((int) p.getId().longValue())));
                    connection.sendTCP(inventoryItemRemoveAnswerPackets.toArray(new Packet[0]));
                });
            }

            S2CDisconnectAnswerPacket disconnectAnswerPacket = new S2CDisconnectAnswerPacket();
            connection.sendTCP(disconnectAnswerPacket);
        }
    }
}
