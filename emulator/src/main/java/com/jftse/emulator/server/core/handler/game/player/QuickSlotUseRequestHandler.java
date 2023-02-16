package com.jftse.emulator.server.core.handler.game.player;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.life.item.ItemFactory;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packet.packets.player.C2SQuickSlotUseRequestPacket;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;
import org.springframework.util.MultiValueMap;

public class QuickSlotUseRequestHandler extends AbstractHandler {
    private C2SQuickSlotUseRequestPacket quickSlotUseRequestPacket;

    public QuickSlotUseRequestHandler() {
    }

    @Override
    public boolean process(Packet packet) {
        quickSlotUseRequestPacket = new C2SQuickSlotUseRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        Player player = connection.getClient().getPlayer();
        Pocket pocket = player.getPocket();
        if (pocket == null)
            return;

        BaseItem baseItem = ItemFactory.getItem(quickSlotUseRequestPacket.getQuickSlotId(), pocket);
        if (baseItem == null)
            return;

        if (baseItem.processPlayer(player)) {
            baseItem.processPocket(pocket);
        }
        sendPackets(baseItem.getPacketsToSend());
    }

    private void sendPackets(MultiValueMap<Long, Packet> packetsToSend) {
        packetsToSend.forEach((playerId, packets) -> {
            final Connection connectionByPlayerId = GameManager.getInstance().getConnectionByPlayerId(playerId);
            if (connectionByPlayerId != null)
                connectionByPlayerId.sendTCP(packets.toArray(Packet[]::new));
        });
    }
}
