package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.core.packet.packets.lobby.room.C2SRoomKickPlayerRequestPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomJoinAnswerPacket;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.List;

public class RoomKickPlayerRequestPacketHandler extends AbstractHandler {
    private C2SRoomKickPlayerRequestPacket roomKickPlayerRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomKickPlayerRequestPacket = new C2SRoomKickPlayerRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null)
            return;

        Room room = connection.getClient().getActiveRoom();

        if (room != null) {
            RoomPlayer playerToKick = room.getRoomPlayerList().stream()
                    .filter(rp -> rp.getPosition() == roomKickPlayerRequestPacket.getPosition())
                    .findAny()
                    .orElse(null);

            if (playerToKick != null) {
                List<Client> clientsInRoom = GameManager.getInstance().getClientsInRoom(room.getRoomId());
                Client client = clientsInRoom.stream()
                        .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(playerToKick.getPlayer().getId()))
                        .findFirst()
                        .orElse(null);

                if (client != null) {
                    Packet answerPacket = new Packet(PacketOperations.S2CRoomLeaveAnswer.getValueAsChar());
                    answerPacket.write(0);
                    connection.getServer().sendToTcp(client.getConnection().getId(), answerPacket);

                    GameManager.getInstance().handleRoomPlayerChanges(client.getConnection(), true);

                    S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -4, (byte) 0, (byte) 0, (byte) 0);
                    connection.getServer().sendToTcp(client.getConnection().getId(), roomJoinAnswerPacket);

                    room.getBannedPlayers().add(client.getPlayer().getId());
                }
            }
        }
    }
}
