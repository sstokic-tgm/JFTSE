package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.server.core.constants.GameMode;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.packet.packets.lobby.room.C2SRoomGameModeChangeRequestPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomInformationPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomListAnswerPacket;
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

        if (changeRoomGameModeRequestPacket.getMode() == GameMode.BATTLE) {
            changeRoomGameModeRequestPacket.setMode((byte) GameMode.GUARDIAN);
        }

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

            GameManager.getInstance().getClientsInLobby().forEach(c -> {
                boolean isActivePlayer = c.getActivePlayer().getId().equals(connection.getClient().getActivePlayer().getId());
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
