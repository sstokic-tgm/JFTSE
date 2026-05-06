package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.matchplay.CMSGReadyToSkip;
import com.jftse.server.core.shared.packets.matchplay.SMSGAllowSkip;

@PacketId(CMSGReadyToSkip.PACKET_ID)
public class GameAnimationReadyToSkipPacketHandler implements PacketHandler<FTConnection, CMSGReadyToSkip> {
    @Override
    public void handle(FTConnection connection, CMSGReadyToSkip packet) {
        FTClient ftClient = connection.getClient();
        if (!ftClient.hasPlayer() || ftClient.getActiveRoom() == null)
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

                SMSGAllowSkip allowSkipPacket = SMSGAllowSkip.builder().result((char) 0).build();
                GameManager.getInstance().sendPacketToAllClientsInSameGameSession(allowSkipPacket, connection);
            }
        }
    }
}
