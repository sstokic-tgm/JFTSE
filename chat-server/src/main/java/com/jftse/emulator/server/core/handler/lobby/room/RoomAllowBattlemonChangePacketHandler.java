package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomChangeAllowBattlemon;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomChangeAllowBattlemon;

@PacketId(CMSGRoomChangeAllowBattlemon.PACKET_ID)
public class RoomAllowBattlemonChangePacketHandler implements PacketHandler<FTConnection, CMSGRoomChangeAllowBattlemon> {
    @Override
    public void handle(FTConnection connection, CMSGRoomChangeAllowBattlemon packet) {
        FTClient client = connection.getClient();
        Room room = client.getActiveRoom();
        if (room != null) {
            byte allowBattlemon = packet.getAllowBattlemon();
            // disable battlemon
            synchronized (room) {
                room.setAllowBattlemon(allowBattlemon);
            }

            SMSGRoomChangeAllowBattlemon response = SMSGRoomChangeAllowBattlemon.builder().allowBattlemon(allowBattlemon).build();
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(response, connection);
        }
    }
}
