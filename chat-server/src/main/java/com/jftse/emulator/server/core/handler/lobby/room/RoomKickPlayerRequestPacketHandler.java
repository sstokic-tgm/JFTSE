package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomKickPlayerRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomJoinAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SRoomKickPlayer)
public class RoomKickPlayerRequestPacketHandler extends AbstractPacketHandler {
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

        FTClient ftClient = (FTClient) connection.getClient();

        final RoomPlayer roomPlayer = ftClient.getRoomPlayer();
        if (roomPlayer == null || !roomPlayer.isMaster())
            return;

        final Room room = ftClient.getActiveRoom();

        if (room != null) {
            RoomPlayer playerToKick = room.getRoomPlayerList().stream()
                    .filter(rp -> rp.getPosition() == roomKickPlayerRequestPacket.getPosition())
                    .findAny()
                    .orElse(null);

            if (playerToKick != null) {
                final List<FTClient> clientsInRoom = GameManager.getInstance().getClientsInRoom(room.getRoomId());
                final FTClient client = clientsInRoom.stream()
                        .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(playerToKick.getPlayer().getId()))
                        .findFirst()
                        .orElse(null);

                if (client != null) {
                    Packet answerPacket = new Packet(PacketOperations.S2CRoomLeaveAnswer);
                    answerPacket.write(0);
                    client.getConnection().sendTCP(answerPacket);

                    GameManager.getInstance().handleRoomPlayerChanges(client.getConnection(), true);

                    S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -4, (byte) 0, (byte) 0, (byte) 0);
                    client.getConnection().sendTCP(roomJoinAnswerPacket);

                    room.getBannedPlayers().add(client.getPlayer().getId());
                }
            }
        }
    }
}
