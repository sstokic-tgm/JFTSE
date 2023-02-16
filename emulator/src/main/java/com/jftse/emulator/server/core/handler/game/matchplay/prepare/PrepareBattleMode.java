package com.jftse.emulator.server.core.handler.game.matchplay.prepare;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.service.WillDamageService;
import com.jftse.emulator.server.networking.packet.Packet;

public class PrepareBattleMode extends AbstractHandler {
    private final WillDamageService willDamageService;

    public PrepareBattleMode() {
        willDamageService = ServiceManager.getInstance().getWillDamageService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActiveRoom() == null || connection.getClient().getActiveGameSession() == null)
            return;

        Room room = connection.getClient().getActiveRoom();
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayBattleGame game = (MatchplayBattleGame) gameSession.getActiveMatchplayGame();
        game.setWillDamages(willDamageService.getWillDamages());

        room.getRoomPlayerList().forEach(roomPlayer -> {
            if (roomPlayer.getPosition() < 4)
                game.getPlayerBattleStates().add(game.createPlayerBattleState(roomPlayer));
        });
    }
}
