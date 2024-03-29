package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.battle.SkillCrystal;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.event.RunnableEventHandler;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayPlaceSkillCrystal;
import com.jftse.emulator.server.core.thread.AbstractTask;
import com.jftse.emulator.server.networking.Connection;

import java.awt.geom.Point2D;

public class PlaceCrystalRandomlyTask extends AbstractTask {
    private final Connection connection;
    private final short gameFieldSide;

    private final RunnableEventHandler runnableEventHandler;

    public PlaceCrystalRandomlyTask(Connection connection, short gameFieldSide) {
        this.connection = connection;
        this.gameFieldSide = gameFieldSide;

        runnableEventHandler = GameManager.getInstance().getRunnableEventHandler();
    }

    public PlaceCrystalRandomlyTask(Connection connection) {
        this.connection = connection;
        this.gameFieldSide = -1;

        runnableEventHandler = GameManager.getInstance().getRunnableEventHandler();
    }

    @Override
    public void run() {
        if (connection.getClient() == null) return;

        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession == null) return;

        MatchplayGame game = gameSession.getActiveMatchplayGame();
        boolean isBattleGame = false;
        if (gameSession.getActiveMatchplayGame() instanceof MatchplayBattleGame)
            isBattleGame = true;

        Point2D point = isBattleGame ? this.getRandomPoint(gameFieldSide) : this.getRandomPoint();

        short crystalId = (short) (isBattleGame ?
                (((MatchplayBattleGame) game).getLastCrystalId() + 1) :
                (((MatchplayGuardianGame) game).getLastCrystalId() + 1));
        if (crystalId > 100) crystalId = 0;

        if (isBattleGame)
            ((MatchplayBattleGame) game).setLastCrystalId(crystalId);
        else
            ((MatchplayGuardianGame) game).setLastCrystalId(crystalId);

        SkillCrystal skillCrystal = new SkillCrystal();
        skillCrystal.setId(crystalId);
        if (isBattleGame)
            ((MatchplayBattleGame) game).getSkillCrystals().add(skillCrystal);
        else
            ((MatchplayGuardianGame) game).getSkillCrystals().add(skillCrystal);

        S2CMatchplayPlaceSkillCrystal placeSkillCrystal = new S2CMatchplayPlaceSkillCrystal(skillCrystal.getId(), point);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(placeSkillCrystal, connection);

        RunnableEvent runnableEvent = null;
        if (isBattleGame)
            runnableEvent = runnableEventHandler.createRunnableEvent(new DespawnCrystalTask(connection, skillCrystal, gameFieldSide), ((MatchplayBattleGame) game).getCrystalDeSpawnInterval());
        else
            runnableEvent = runnableEventHandler.createRunnableEvent(new DespawnCrystalTask(connection, skillCrystal), ((MatchplayGuardianGame) game).getCrystalDeSpawnInterval());

        gameSession.getRunnableEvents().add(runnableEvent);
    }

    private Point2D getRandomPoint() {
        int negator = (int) (Math.random() * 2) == 0 ? -1 : 1;
        float xPos = (float) (Math.random() * 60) * negator;
        float yPos = (short) (Math.random() * 120) * -1;
        yPos = Math.abs(yPos) < 10 ? -10 : yPos;
        return new Point2D.Float(xPos, yPos);
    }

    private Point2D getRandomPoint(short gameFieldSide) {
        int negator = (int) (Math.random() * 2) == 0 ? -1 : 1;
        float xPos = (float) (Math.random() * 60) * negator;

        float yPos = (short) (Math.random() * 120);
        if (gameFieldSide == GameFieldSide.RedTeam) {
            yPos = (short) (Math.random() * 120) * -1;
            yPos = Math.abs(yPos) < 10 ? -10 : yPos;
        } else if (gameFieldSide == GameFieldSide.BlueTeam) {
            yPos = Math.abs(yPos) < 10 ? 10 : yPos;
        }

        return new Point2D.Float(xPos, yPos);
    }
}
