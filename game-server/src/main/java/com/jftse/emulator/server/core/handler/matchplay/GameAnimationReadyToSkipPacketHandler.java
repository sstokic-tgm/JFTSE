package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;

@PacketOperationIdentifier(PacketOperations.C2SGameAnimationSkipReady)
public class GameAnimationReadyToSkipPacketHandler extends AbstractPacketHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null || ftClient.getActiveRoom() == null)
            return;

        Player player = ftClient.getPlayer();
        Room room = ftClient.getActiveRoom();
        room.getRoomPlayerList().stream()
                .filter(x -> x.getPlayerId().equals(player.getId()))
                .findFirst()
                .ifPresent(rp -> {
                    synchronized (rp) {
                        rp.setGameAnimationSkipReady(true);
                    }
                });

        boolean allPlayerCanSkipAnimation = room.getRoomPlayerList().stream().allMatch(RoomPlayer::isGameAnimationSkipReady);

        if (allPlayerCanSkipAnimation) {
            Packet gameAnimationAllowSkipPacket = new Packet(PacketOperations.S2CGameAnimationAllowSkip.getValue());
            gameAnimationAllowSkipPacket.write((char) 0);
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null)
                    c.getConnection().sendTCP(gameAnimationAllowSkipPacket);
            });
        }
    }
}
