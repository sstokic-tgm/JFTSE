package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayPlaceSkillCrystal;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.matchplay.battle.SkillCrystal;
import com.jftse.server.core.thread.AbstractTask;

import java.awt.geom.Point2D;

public class PlaceCrystalRandomlyTask extends AbstractTask {
    private final FTConnection connection;
    private final short gameFieldSide;

    private final EventHandler eventHandler;

    public PlaceCrystalRandomlyTask(FTConnection connection, short gameFieldSide) {
        this.connection = connection;
        this.gameFieldSide = gameFieldSide;

        eventHandler = GameManager.getInstance().getEventHandler();
    }

    public PlaceCrystalRandomlyTask(FTConnection connection) {
        this.connection = connection;
        this.gameFieldSide = -1;

        eventHandler = GameManager.getInstance().getEventHandler();
    }

    @Override
    public void run() {
        if (connection.getClient() == null) return;

        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession == null) return;

        MatchplayGame game = gameSession.getMatchplayGame();
        boolean isBattleGame = false;
        if (gameSession.getMatchplayGame() instanceof MatchplayBattleGame)
            isBattleGame = true;

        Point2D point = isBattleGame ? this.getRandomPoint(gameFieldSide) : this.getRandomPoint();

        short crystalId = (short) (isBattleGame ?
                (((MatchplayBattleGame) game).getLastCrystalId().incrementAndGet()) :
                (((MatchplayGuardianGame) game).getLastCrystalId().incrementAndGet()));
        if (crystalId > 100) {
            crystalId = 0;
            if (isBattleGame)
                ((MatchplayBattleGame) game).getLastCrystalId().set(0);
            else
                ((MatchplayGuardianGame) game).getLastCrystalId().set(0);
        }

        SkillCrystal skillCrystal = new SkillCrystal();
        skillCrystal.setId(crystalId);
        if (isBattleGame)
            ((MatchplayBattleGame) game).getSkillCrystals().add(skillCrystal);
        else
            ((MatchplayGuardianGame) game).getSkillCrystals().add(skillCrystal);

        S2CMatchplayPlaceSkillCrystal placeSkillCrystal = new S2CMatchplayPlaceSkillCrystal(skillCrystal.getId(), point);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(placeSkillCrystal, connection);

        RunnableEvent runnableEvent;
        if (isBattleGame)
            runnableEvent = eventHandler.createRunnableEvent(new DespawnCrystalTask(connection, skillCrystal, gameFieldSide), ((MatchplayBattleGame) game).getCrystalDeSpawnInterval().get());
        else
            runnableEvent = eventHandler.createRunnableEvent(new DespawnCrystalTask(connection, skillCrystal), ((MatchplayGuardianGame) game).getCrystalDeSpawnInterval().get());

        eventHandler.push(runnableEvent);
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
