package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.life.event.GameEventBus;
import com.jftse.emulator.server.core.life.event.GameEventType;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayGiveSpecificSkill;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayLetCrystalDisappear;
import com.jftse.emulator.server.core.task.PlaceCrystalRandomlyTask;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.battle.SkillDropRate;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.server.core.matchplay.battle.SkillCrystal;
import com.jftse.server.core.service.SkillDropRateService;
import com.jftse.server.core.shared.packets.matchplay.CMSGPlayerPickupCrystal;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

@Log4j2
@PacketId(CMSGPlayerPickupCrystal.PACKET_ID)
public class PlayerPickingUpCrystalHandler implements PacketHandler<FTConnection, CMSGPlayerPickupCrystal> {
    private final SkillDropRateService skillDropRateService;

    private final EventHandler eventHandler;

    private static final int REBIRTH_SKILL_ID = 4;
    private static final int REBIRTH_CHANCE_PERCENT = 35;

    public PlayerPickingUpCrystalHandler() {
        skillDropRateService = ServiceManager.getInstance().getSkillDropRateService();

        eventHandler = GameManager.getInstance().getEventHandler();
    }

    @Override
    public void handle(FTConnection connection, CMSGPlayerPickupCrystal packet) {
        FTClient ftClient = connection.getClient();
        if (!ftClient.hasPlayer() || ftClient.getActiveGameSession() == null || ftClient.getActiveRoom() == null)
            return;

        FTPlayer player = ftClient.getPlayer();
        final Room activeRoom = ftClient.getActiveRoom();
        final GameSession gameSession = ftClient.getActiveGameSession();

        final RoomPlayer roomPlayer = ftClient.getRoomPlayer();
        if (roomPlayer == null)
            return;

        short playerPosition = roomPlayer.getPosition();
        Queue<SkillCrystal> pickedUpSkillCrystals = roomPlayer.getPickedUpSkillCrystals();

        final MatchplayGame game = gameSession.getMatchplayGame();
        if (game == null)
            return;

        boolean isBattleGame = game instanceof MatchplayBattleGame;

        SkillCrystal skillCrystal = isBattleGame ?
                ((MatchplayBattleGame) game).getSkillCrystals().stream()
                        .filter(x -> x.getId() == packet.getCrystalId())
                        .findFirst()
                        .orElse(null) :
                ((MatchplayGuardianGame) game).getSkillCrystals().stream()
                        .filter(x -> x.getId() == packet.getCrystalId())
                        .findFirst()
                        .orElse(null);

        if (skillCrystal == null) {
            return;
        }

        if (isBattleGame)
            ((MatchplayBattleGame) game).getSkillCrystals().remove(skillCrystal);
        else
            ((MatchplayGuardianGame) game).getSkillCrystals().remove(skillCrystal);

        short gameFieldSide = -1;
        boolean isRedTeam = game.isRedTeam(playerPosition);
        if (isBattleGame)
            gameFieldSide = game.isRedTeam(playerPosition) ? GameFieldSide.RedTeam : GameFieldSide.BlueTeam;

        if (skillCrystal.getPickedUpByPlayerId() == -1) {
            PlayerBattleState playerBattleState = isBattleGame ?
                    ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                            .filter(x -> isRedTeam == game.isRedTeam(x.getPosition()) && x.isDead())
                            .findFirst()
                            .orElse(null) :
                    ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                            .filter(x -> isRedTeam == game.isRedTeam(x.getPosition()) && x.isDead())
                            .findFirst()
                            .orElse(null);
            boolean levelRequired = !isBattleGame && activeRoom.getRoomPlayerList().stream()
                    .allMatch(p -> p.getLevel() >= 65);

            int randomSkillIndex = this.getRandomPlayerSkill(player.getLevel(), playerBattleState, levelRequired);
            skillCrystal.setPickedUpByPlayerId(player.getId());
            skillCrystal.setSkillIndex(randomSkillIndex);

            if (!pickedUpSkillCrystals.offer(skillCrystal)) {
                pickedUpSkillCrystals.poll();
                pickedUpSkillCrystals.offer(skillCrystal);
            }

            S2CMatchplayLetCrystalDisappear letCrystalDisappear = new S2CMatchplayLetCrystalDisappear((short) skillCrystal.getId());
            gameSession.getClients().forEach(c -> {
                if (c.getConnection().getId() != connection.getId()) {
                    c.getConnection().sendTCP(letCrystalDisappear);
                }
            });

            S2CMatchplayGiveSpecificSkill response = new S2CMatchplayGiveSpecificSkill((short) skillCrystal.getId(), playerPosition, randomSkillIndex);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(response, connection);

            GameEventBus.call(GameEventType.MP_PLAYER_PICKING_UP_CRYSTAL, ftClient, skillCrystal, randomSkillIndex);
        }

        PlaceCrystalRandomlyTask placeCrystalRandomlyTask = isBattleGame ? new PlaceCrystalRandomlyTask(connection, gameFieldSide) : new PlaceCrystalRandomlyTask(connection);
        long crystalSpawnInterval = isBattleGame ? ((MatchplayBattleGame) game).getCrystalSpawnInterval().get() : ((MatchplayGuardianGame) game).getCrystalSpawnInterval().get();
        RunnableEvent runnableEvent = eventHandler.createRunnableEvent(placeCrystalRandomlyTask, crystalSpawnInterval);
        gameSession.getFireables().push(runnableEvent);
        eventHandler.offer(runnableEvent);
    }

    private int getRandomPlayerSkill(int playerLevel, PlayerBattleState otherPlayerBattleStateDead, boolean levelRequired) {
        final SkillDropRate skillDropRate = skillDropRateService.findSkillDropRateByPlayerLevel(playerLevel);
        if (skillDropRate == null) {
            return 0;
        }

        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        if (otherPlayerBattleStateDead != null) { // if someone is dead
            if (rnd.nextInt(100) < REBIRTH_CHANCE_PERCENT) {
                return REBIRTH_SKILL_ID;
            }
        }

        List<Integer> weights = skillDropRateService.getDropRatesForSkillDropRate(skillDropRate);
        int totalWeight = weights.stream().mapToInt(Integer::intValue).sum();
        if (totalWeight <= 0) {
            return 0;
        }

        if (totalWeight != 100) {
            log.warn("Drop table total is {}, expected 100. level={}, rates={}", totalWeight, playerLevel, skillDropRate.getDropRates());
        }

        int roll = rnd.nextInt(totalWeight);
        int cumulativeWeight = 0;
        int selectedSkillId = 0;
        for (int i = 0; i < weights.size(); i++) {
            cumulativeWeight += weights.get(i);
            if (roll < cumulativeWeight) {
                selectedSkillId = i;
                break;
            }
        }

        if (levelRequired && selectedSkillId == 2) {
            if (rnd.nextInt(100) < 26) {
                selectedSkillId = 55;
            }
        }

        return selectedSkillId;
    }
}
