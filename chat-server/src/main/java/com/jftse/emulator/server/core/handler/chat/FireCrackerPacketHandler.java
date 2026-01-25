package com.jftse.emulator.server.core.handler.chat;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.life.item.ItemFactory;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.chat.CMSGFireCracker;
import com.jftse.server.core.shared.packets.chat.SMSGFireCracker;

@PacketId(CMSGFireCracker.PACKET_ID)
public class FireCrackerPacketHandler implements PacketHandler<FTConnection, CMSGFireCracker> {
    @Override
    public void handle(FTConnection connection, CMSGFireCracker fireCrackerReqPacket) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer == null)
            return;

        if (roomPlayer.getPosition() != fireCrackerReqPacket.getPosition())
            return;

        FTPlayer player = client.getPlayer();

        BaseItem baseItem = ItemFactory.getItem(fireCrackerReqPacket.getPlayerPocketId(), player.getPocketId());
        if (baseItem == null)
            return;

        boolean valid = false;
        if (baseItem.processPlayer(player)) {
            valid = baseItem.processPocket(player.getPocketId());
        }

        if (valid) {
            SMSGFireCracker fireCrackerAnswer = SMSGFireCracker.builder()
                    .fireCrackerType(fireCrackerReqPacket.getFireCrackerType())
                    .position(roomPlayer.getPosition())
                    .build();
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(fireCrackerAnswer, client.getConnection());

            baseItem.getPacketsToSend().forEach((playerId, packets) -> {
                packets.forEach(connection::sendTCP);
            });
        }
    }
}
