package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomKickPlayer;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomJoin;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomLeave;

import java.util.List;

@PacketId(CMSGRoomKickPlayer.PACKET_ID)
public class RoomKickPlayerRequestPacketHandler implements PacketHandler<FTConnection, CMSGRoomKickPlayer> {
    @Override
    public void handle(FTConnection connection, CMSGRoomKickPlayer packet) {
        FTClient ftClient = connection.getClient();

        final RoomPlayer roomPlayer = ftClient.getRoomPlayer();
        if (roomPlayer == null || !roomPlayer.isMaster())
            return;

        final Room room = ftClient.getActiveRoom();

        if (room != null) {
            RoomPlayer playerToKick = room.getRoomPlayerList().stream()
                    .filter(rp -> rp.getPosition() == packet.getPosition())
                    .findAny()
                    .orElse(null);

            if (playerToKick != null) {
                final List<FTClient> clientsInRoom = GameManager.getInstance().getClientsInRoom(room.getRoomId());
                final FTClient client = clientsInRoom.stream()
                        .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(playerToKick.getPlayer().getId()))
                        .findFirst()
                        .orElse(null);

                if (client != null) {
                    SMSGRoomLeave answer = SMSGRoomLeave.builder().result((short) 0).build();
                    client.getConnection().sendTCP(answer);

                    GameManager.getInstance().handleRoomPlayerChanges(client.getConnection(), true);

                    SMSGRoomJoin roomJoinAnswerPacket = SMSGRoomJoin.builder()
                            .result((char) -4)
                            .roomType((byte) 0)
                            .mode((byte) 0)
                            .mapId((byte) 0)
                            .build();
                    client.getConnection().sendTCP(roomJoinAnswerPacket);

                    room.getBannedPlayers().add(client.getPlayer().getId());
                }
            }
        }
    }
}
