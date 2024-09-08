package com.jftse.emulator.server.core.handler.enchant;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.enchant.C2SEnchantAnnouncePacket;
import com.jftse.emulator.server.core.packets.item.S2CPlayerAnnouncePacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.concurrent.ConcurrentLinkedDeque;

@PacketOperationIdentifier(PacketOperations.C2SEnchantAnnounce)
public class EnchantAnnounceHandler extends AbstractPacketHandler {
    private C2SEnchantAnnouncePacket packet;

    @Override
    public boolean process(Packet packet) {
        this.packet = new C2SEnchantAnnouncePacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null) {
            return;
        }
        Player player = client.getPlayer();
        if (player == null) {
            return;
        }
        String playerName = player.getName();
        String message = packet.getMessage();

        int nameEndIndex = message.indexOf('\'', 1);
        String msgPlayerName = message.substring(1, nameEndIndex);
        String msg = message.substring(nameEndIndex + 1);

        if (!playerName.equalsIgnoreCase(msgPlayerName)) {
            return;
        }

        S2CPlayerAnnouncePacket announcePacket = new S2CPlayerAnnouncePacket(msgPlayerName, packet.getTextSize(), packet.getTextColor(), msg);
        final ConcurrentLinkedDeque<FTClient> clients = GameManager.getInstance().getClients();
        clients.stream()
                .filter(c -> c.getConnection() != null && c.getActiveGameSession() == null)
                .map(FTClient::getConnection)
                .forEach(c -> c.sendTCP(announcePacket));
    }
}
