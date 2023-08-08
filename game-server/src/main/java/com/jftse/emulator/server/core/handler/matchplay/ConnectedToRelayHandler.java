package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.concurrent.ConcurrentLinkedDeque;

@PacketOperationIdentifier(PacketOperations.C2SMatchplayConnectedToRelay)
public class ConnectedToRelayHandler extends AbstractPacketHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null) {
            return;
        }

        RoomPlayer roomPlayer = ftClient.getRoomPlayer();
        if (roomPlayer == null || !roomPlayer.getConnectedToRelay().compareAndSet(false, true)) {
            Packet answer = new Packet(PacketOperations.S2CMatchplayAckRelayConnection);
            answer.write((byte) 1);
            connection.sendTCP(answer);

            Room room = ftClient.getActiveRoom();
            if (room != null) {
                synchronized (room) {
                    room.setStatus(RoomStatus.RelayConnectionFailed);
                }
            }
            return;
        }

        Room room = ftClient.getActiveRoom();
        synchronized (room) {
            ConcurrentLinkedDeque<RoomPlayer> roomPlayerList = room.getRoomPlayerList();
            if (roomPlayerList.stream().allMatch(rp -> rp.getConnectedToRelay().get())) {
                room.setStatus(RoomStatus.RelayConnectionSuccess);
            }
        }
    }
}
