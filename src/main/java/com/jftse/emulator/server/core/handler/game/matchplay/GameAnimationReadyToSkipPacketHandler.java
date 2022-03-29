package com.jftse.emulator.server.core.handler.game.matchplay;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class GameAnimationReadyToSkipPacketHandler extends AbstractHandler {
    private final PlayerService playerService;

    public GameAnimationReadyToSkipPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null || connection.getClient().getActiveRoom() == null)
            return;

        Player player = playerService.findById(connection.getClient().getActivePlayer().getId());
        Room room = connection.getClient().getActiveRoom();
        room.getRoomPlayerList().stream()
                .filter(x -> x.getPlayer().getId().equals(player.getId()))
                .findFirst()
                .ifPresent(rp -> {
                    synchronized (rp) {
                        rp.setGameAnimationSkipReady(true);
                    }
                });

        boolean allPlayerCanSkipAnimation = room.getRoomPlayerList().stream().allMatch(RoomPlayer::isGameAnimationSkipReady);

        if (allPlayerCanSkipAnimation) {
            Packet gameAnimationAllowSkipPacket = new Packet(PacketOperations.S2CGameAnimationAllowSkip.getValueAsChar());
            gameAnimationAllowSkipPacket.write((char) 0);
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null && c.getConnection().isConnected())
                    c.getConnection().sendTCP(gameAnimationAllowSkipPacket);
            });
        }
    }
}
