package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.matchplay.CMSGMatchRelayReady;

@PacketId(CMSGMatchRelayReady.PACKET_ID)
public class ConnectedToRelayHandler implements PacketHandler<FTConnection, CMSGMatchRelayReady> {
    @Override
    public void handle(FTConnection connection, CMSGMatchRelayReady packet) {
        FTClient client = connection.getClient();
        if (client == null || !client.hasPlayer())
            return;

        Room room = client.getActiveRoom();
        GameSession session = client.getActiveGameSession();
        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (room == null || session == null || roomPlayer == null || !session.getClients().contains(client))
            return;

        synchronized (room) {
            if (room.getStatus() != RoomStatus.StartingGame)
                return;

            roomPlayer.getConnectedToRelay().set(true);
            // All registered clients, including spectators, must have their relay connection.
            boolean allConnected = session.getClients().stream().allMatch(c -> {
                RoomPlayer player = c.getRoomPlayer();
                return c.getActiveRoom() == room && c.getActiveGameSession() == session
                        && player != null && player.getConnectedToRelay().get();
            });
            if (allConnected) {
                room.setStatus(RoomStatus.RelayConnectionSuccess);
            }
        }
    }
}
