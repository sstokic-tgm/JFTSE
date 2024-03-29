package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.packet.packets.lobby.room.C2SRoomGameModeChangeRequestPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomInformationPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomListAnswerPacket;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class GameModeChangePacketHandler extends AbstractHandler {
    private C2SRoomGameModeChangeRequestPacket changeRoomGameModeRequestPacket;

    @Override
    public boolean process(Packet packet) {
        changeRoomGameModeRequestPacket = new C2SRoomGameModeChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Room room = connection.getClient().getActiveRoom();

        if (room != null) {
            synchronized (room) {
                room.setMode(changeRoomGameModeRequestPacket.getMode());
            }

            S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null && c.getConnection().isConnected()) {
                    c.getConnection().sendTCP(roomInformationPacket);
                }
            });

            Player player = connection.getClient().getPlayer();
            GameManager.getInstance().getClientsInLobby().forEach(c -> {
                boolean isActivePlayer = c.getPlayer() != null && c.getPlayer().getId().equals(player.getId());
                if (isActivePlayer)
                    return;

                if (c.getConnection() != null && c.getConnection().isConnected()) {
                    S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(GameManager.getInstance().getFilteredRoomsForClient(c));
                    c.getConnection().sendTCP(roomListAnswerPacket);
                }
            });
        }
    }
}
