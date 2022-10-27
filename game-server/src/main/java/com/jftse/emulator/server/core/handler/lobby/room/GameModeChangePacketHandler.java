package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomGameModeChangeRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomInformationPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomListAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SRoomGameModeChange)
public class GameModeChangePacketHandler extends AbstractPacketHandler {
    private C2SRoomGameModeChangeRequestPacket changeRoomGameModeRequestPacket;

    @Override
    public boolean process(Packet packet) {
        changeRoomGameModeRequestPacket = new C2SRoomGameModeChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = connection.getClient();
        Room room = client.getActiveRoom();

        if (room != null) {
            synchronized (room) {
                room.setMode(changeRoomGameModeRequestPacket.getMode());
            }

            S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null) {
                    c.getConnection().sendTCP(roomInformationPacket);
                }
            });

            Player player = client.getPlayer();
            GameManager.getInstance().getClientsInLobby().forEach(c -> {
                boolean isActivePlayer = c.getPlayer() != null && c.getPlayer().getId().equals(player.getId());
                if (isActivePlayer)
                    return;

                if (c.getConnection() != null) {
                    S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(GameManager.getInstance().getFilteredRoomsForClient(c));
                    c.getConnection().sendTCP(roomListAnswerPacket);
                }
            });
        }
    }
}
