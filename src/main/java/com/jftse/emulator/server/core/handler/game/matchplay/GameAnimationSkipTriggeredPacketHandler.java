package com.jftse.emulator.server.core.handler.game.matchplay;

import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.handler.game.matchplay.start.*;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ThreadManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBasicGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CGameDisplayPlayerStatsPacket;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CGameSetNameColorAndRemoveBlackBar;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GameAnimationSkipTriggeredPacketHandler extends AbstractHandler {
    private Packet packet;

    @Override
    public boolean process(Packet packet) {
        this.packet = packet;
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActiveRoom() == null
                || connection.getClient().getActivePlayer() == null || connection.getClient().getActiveGameSession() == null)
            return;

        Room room = connection.getClient().getActiveRoom();
        if (room.getStatus() != RoomStatus.InitializingGame) {
            return;
        }

        Optional<RoomPlayer> roomPlayer = room.getRoomPlayerList().stream()
                .filter(x -> x.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                .findFirst();

        if (roomPlayer.isPresent()) {
            Packet gameAnimationSkipPacket = new Packet(PacketOperations.S2CGameAnimationSkip.getValueAsChar());
            gameAnimationSkipPacket.write((char) 0);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(gameAnimationSkipPacket, connection);

            S2CGameDisplayPlayerStatsPacket playerStatsPacket = new S2CGameDisplayPlayerStatsPacket(connection.getClient().getActiveRoom());
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(playerStatsPacket, connection);

            synchronized (room) {
                room.setStatus(RoomStatus.Running);
            }

            ThreadManager.getInstance().schedule(() -> {
                Client client = connection.getClient();
                if (client == null) return;

                Room threadRoom = client.getActiveRoom();
                if (threadRoom == null || threadRoom.getStatus() != RoomStatus.Running) {
                    return;
                }

                S2CGameSetNameColorAndRemoveBlackBar setNameColorAndRemoveBlackBarPacket = new S2CGameSetNameColorAndRemoveBlackBar(room);
                GameManager.getInstance().sendPacketToAllClientsInSameGameSession(setNameColorAndRemoveBlackBarPacket, connection);

                AbstractHandler handler;
                GameSession gameSession = connection.getClient().getActiveGameSession();
                MatchplayGame game = gameSession.getActiveMatchplayGame();
                if (game == null)
                    return;

                if (game instanceof MatchplayBasicGame) {
                    handler = new StartBasicModeHandler();
                } else if (game instanceof MatchplayBattleGame) {
                    handler = new StartBattleModeHandler();
                } else if (game instanceof MatchplayGuardianGame) {
                    handler = new StartGuardianModeHandler();
                } else { // default
                    handler = new AbstractHandler() {
                        @Override
                        public boolean process(Packet packet) {
                            return false;
                        }

                        @Override
                        public void handle() {
                            // empty
                        }
                    };
                }

                try {
                    handler.setConnection(connection);
                    if (handler.process(packet))
                        handler.handle();
                } catch (Exception e) {
                    connection.notifyException(e);
                }
            }, 8, TimeUnit.SECONDS);
        }
    }
}
