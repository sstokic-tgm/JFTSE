package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.common.utilities.StreamUtils;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.emulator.server.core.service.PlayerPocketService;
import com.jftse.entities.database.model.account.Account;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.S2CDisconnectAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.authserver.S2CLoginAnswerPacket;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class DisconnectPacketHandler extends AbstractHandler {
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
        if (connection.getClient() == null)
            return;

        if (connection.getClient().getPlayer() != null) {
            Player player = connection.getClient().getPlayer();
            // reset pocket
            List<PlayerPocket> playerPocketList = playerPocketService.getPlayerPocketItems(player.getPocket());
            StreamUtils.batches(playerPocketList, 20).forEach(pocketList -> {
                List<Packet> inventoryItemRemoveAnswerPackets = new ArrayList<>();
                pocketList.forEach(p -> inventoryItemRemoveAnswerPackets.add(new S2CInventoryItemRemoveAnswerPacket((int) p.getId().longValue())));
                connection.sendTCP(inventoryItemRemoveAnswerPackets.toArray(new Packet[0]));
            });
        }

        if (connection.getClient().getAccount() != null) {
            // reset status
            Account account = connection.getClient().getAccount();
            if (account.getStatus().shortValue() != S2CLoginAnswerPacket.ACCOUNT_BLOCKED_USER_ID) {
                account.setStatus((int) S2CLoginAnswerPacket.SUCCESS);
                connection.getClient().saveAccount(account);
            }
        }

        S2CDisconnectAnswerPacket disconnectAnswerPacket = new S2CDisconnectAnswerPacket();
        connection.sendTCP(disconnectAnswerPacket);
    }
}
