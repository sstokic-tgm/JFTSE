package com.jftse.emulator.server.core.handler.matchplay.prepare;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.service.WillDamageService;

public class PrepareBattleMode extends AbstractPacketHandler {
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
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getActiveRoom() == null || ftClient.getActiveGameSession() == null)
            return;

        Room room = ftClient.getActiveRoom();
        GameSession gameSession = ftClient.getActiveGameSession();
        MatchplayBattleGame game = (MatchplayBattleGame) gameSession.getMatchplayGame();
        game.setWillDamages(willDamageService.getWillDamages());

        room.getRoomPlayerList().forEach(roomPlayer -> {
            if (roomPlayer.getPosition() < 4)
                game.getPlayerBattleStates().add(game.createPlayerBattleState(roomPlayer));
        });
    }
}
