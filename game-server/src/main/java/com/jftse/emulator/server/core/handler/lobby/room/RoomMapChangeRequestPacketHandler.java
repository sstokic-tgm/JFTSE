package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomChangeMap;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomChangeMap;

@PacketId(CMSGRoomChangeMap.PACKET_ID)
public class RoomMapChangeRequestPacketHandler implements PacketHandler<FTConnection, CMSGRoomChangeMap> {
    @Override
    public void handle(FTConnection connection, CMSGRoomChangeMap packet) {
        FTClient client = connection.getClient();
        Room room = client.getActiveRoom();
        if (room != null) {
            synchronized (room) {
                if (ConfigService.getInstance().getValue("game.map.allow.snowmoon", false) && room.getMode() == GameMode.GUARDIAN) {
                    if (room.getPreviousMap() == 3 && packet.getMap() == 5) {
                        packet.setMap((byte) 4);
                    }
                    if (room.getPreviousMap() == 5 && packet.getMap() == 3) {
                        packet.setMap((byte) 4);
                    }
                }
                room.setPreviousMap(packet.getMap());
                room.setMap(packet.getMap());

                if (room.isRandomGuardians() || room.isHardMode() || room.isArcade()) {
                    if (room.isRandomGuardians()) {
                        room.setRandomGuardians(false);
                    }
                    if (room.isHardMode()) {
                        room.setHardMode(false);
                    }
                    if (room.isArcade()) {
                        room.setArcade(false);
                    }
                }
            }

            SMSGRoomChangeMap answer = SMSGRoomChangeMap.builder().map(packet.getMap()).build();
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null) {
                    c.getConnection().sendTCP(answer);
                }
            });
        }
    }
}
