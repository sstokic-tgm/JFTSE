package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomMapChangeRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomMapChangeAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SRoomMapChange)
public class RoomMapChangeRequestPacketHandler extends AbstractPacketHandler {
    private C2SRoomMapChangeRequestPacket roomMapChangeRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomMapChangeRequestPacket = new C2SRoomMapChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        Room room = client.getActiveRoom();
        if (room != null) {
            synchronized (room) {
                if (ConfigService.getInstance().getValue("game.map.allow.snowmoon", false) && room.getMode() == GameMode.GUARDIAN) {
                    if (room.getPreviousMap() == 3 && roomMapChangeRequestPacket.getMap() == 5) {
                        roomMapChangeRequestPacket.setMap((byte) 4);
                    }
                    if (room.getPreviousMap() == 5 && roomMapChangeRequestPacket.getMap() == 3) {
                        roomMapChangeRequestPacket.setMap((byte) 4);
                    }
                }
                room.setPreviousMap(roomMapChangeRequestPacket.getMap());
                room.setMap(roomMapChangeRequestPacket.getMap());

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

            S2CRoomMapChangeAnswerPacket roomMapChangeAnswerPacket = new S2CRoomMapChangeAnswerPacket(roomMapChangeRequestPacket.getMap());
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null) {
                    c.getConnection().sendTCP(roomMapChangeAnswerPacket);
                }
            });
        }
    }
}
