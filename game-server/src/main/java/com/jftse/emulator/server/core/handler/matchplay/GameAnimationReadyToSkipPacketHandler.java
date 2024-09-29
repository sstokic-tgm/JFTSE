package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SGameAnimationSkipReady)
public class GameAnimationReadyToSkipPacketHandler extends AbstractPacketHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null || ftClient.getActiveRoom() == null)
            return;

        Room room = ftClient.getActiveRoom();
        int roomStatus = room.getStatus();

        RoomPlayer roomPlayer = ftClient.getRoomPlayer();
        if (roomPlayer != null) {
            if (!roomPlayer.isGameAnimationSkipReady()) {
                roomPlayer.setGameAnimationSkipReady(true);
            }
        }

        boolean allPlayerCanSkipAnimation = room.getRoomPlayerList().stream().allMatch(RoomPlayer::isGameAnimationSkipReady);

        if (allPlayerCanSkipAnimation && roomStatus == RoomStatus.InitializingGame) {
            if (room.getStatus() == roomStatus) {
                room.setStatus(RoomStatus.AnimationSkipped);

                Packet gameAnimationAllowSkipPacket = new Packet(PacketOperations.S2CGameAnimationAllowSkip);
                gameAnimationAllowSkipPacket.write((char) 0);
                GameManager.getInstance().sendPacketToAllClientsInSameGameSession(gameAnimationAllowSkipPacket, ftClient.getConnection());
            }
        }
    }
}
