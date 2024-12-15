package com.jftse.emulator.server.core.handler.chat;

import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.life.item.ItemFactory;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.C2SFireCrackerReqPacket;
import com.jftse.emulator.server.core.packets.chat.S2CFireCrackerAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SFireCrackerRequest)
public class FireCrackerPacketHandler extends AbstractPacketHandler {
    private C2SFireCrackerReqPacket fireCrackerReqPacket;

    public FireCrackerPacketHandler() {

    }

    @Override
    public boolean process(Packet packet) {
        fireCrackerReqPacket = new C2SFireCrackerReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null)
            return;

        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer == null)
            return;

        if (roomPlayer.getPosition() != fireCrackerReqPacket.getPosition())
            return;

        Player player = roomPlayer.getPlayer();
        Pocket pocket = player.getPocket();
        if (pocket == null)
            return;

        BaseItem baseItem = ItemFactory.getItem(fireCrackerReqPacket.getPlayerPocketId(), pocket);
        if (baseItem == null)
            return;

        boolean valid = false;
        if (baseItem.processPlayer(player)) {
            valid = baseItem.processPocket(pocket);
        }

        if (valid) {
            S2CFireCrackerAnswerPacket fireCrackerAnswer = new S2CFireCrackerAnswerPacket(fireCrackerReqPacket.getFireCrackerType(), roomPlayer.getPosition());
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(fireCrackerAnswer, client.getConnection());

            baseItem.getPacketsToSend().forEach((playerId, packets) -> {
                packets.forEach(connection::sendTCP);
            });
        }
    }
}
