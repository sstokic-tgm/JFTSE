package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.common.utilities.StringTokenizer;
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
import com.jftse.emulator.server.core.task.PlaceCrystalRandomlyTask;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.battle.SkillDropRate;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.server.core.matchplay.battle.SkillCrystal;
import com.jftse.server.core.matchplay.battle.SkillDrop;
import com.jftse.server.core.service.SkillDropRateService;
import com.jftse.server.core.shared.packets.matchplay.CMSGPlayerPickupCrystal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@PacketId(CMSGPlayerPickupCrystal.PACKET_ID)
public class PlayerPickingUpCrystalHandler implements PacketHandler<FTConnection, CMSGPlayerPickupCrystal> {
    private final SkillDropRateService skillDropRateService;

    private final EventHandler eventHandler;

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

        final RoomPlayer roomPlayer = ftClient.getRoomPlayer();
        if (roomPlayer == null)
            return;

        final Room activeRoom = ftClient.getActiveRoom();
        if (activeRoom == null)
            return;

        short playerPosition = roomPlayer.getPosition();
        final GameSession gameSession = ftClient.getActiveGameSession();
        if (gameSession == null)
            return;

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

        if (skillCrystal != null) {
            short gameFieldSide = -1;
            boolean isRedTeam = game.isRedTeam(playerPosition);
            if (isBattleGame)
                gameFieldSide = game.isRedTeam(playerPosition) ? GameFieldSide.RedTeam : GameFieldSide.BlueTeam;

            PlayerBattleState playerBattleState = isBattleGame ?
                    ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                            .filter(x -> isRedTeam == game.isRedTeam(x.getPosition()) && x.isDead()
                                    || !isRedTeam == !game.isRedTeam(x.getPosition()) && x.isDead())
                            .findFirst()
                            .orElse(null) :
                    ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                            .filter(x -> isRedTeam == game.isRedTeam(x.getPosition()) && x.isDead()
                                    || !isRedTeam == !game.isRedTeam(x.getPosition()) && x.isDead())
                            .findFirst()
                            .orElse(null);
            boolean levelRequired = !isBattleGame && activeRoom.getRoomPlayerList().stream()
                    .allMatch(p -> p.getLevel() >= 65);

            int randomSkillIndex = this.getRandomPlayerSkill(player.getLevel(), playerBattleState, levelRequired);
            S2CMatchplayGiveSpecificSkill response = new S2CMatchplayGiveSpecificSkill(packet.getCrystalId(), playerPosition, randomSkillIndex);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(response, connection);

            GameEventBus.call(GameEventType.MP_PLAYER_PICKING_UP_CRYSTAL, ftClient, skillCrystal, randomSkillIndex);

            if (isBattleGame) {
                ((MatchplayBattleGame) game).getSkillCrystals().removeIf(sc -> sc.getId() == skillCrystal.getId());
            } else {
                ((MatchplayGuardianGame) game).getSkillCrystals().removeIf(sc -> sc.getId() == skillCrystal.getId());
            }

            PlaceCrystalRandomlyTask placeCrystalRandomlyTask = isBattleGame ? new PlaceCrystalRandomlyTask(connection, gameFieldSide) : new PlaceCrystalRandomlyTask(connection);
            long crystalSpawnInterval = isBattleGame ? ((MatchplayBattleGame) game).getCrystalSpawnInterval().get() : ((MatchplayGuardianGame) game).getCrystalSpawnInterval().get();

            RunnableEvent runnableEvent = eventHandler.createRunnableEvent(placeCrystalRandomlyTask, crystalSpawnInterval);
            gameSession.getFireables().push(runnableEvent);
            eventHandler.offer(runnableEvent);
        }
    }

    private int getRandomPlayerSkill(int playerLevel, PlayerBattleState otherPlayerBattleStateDead, boolean levelRequired) {
        final SkillDropRate skillDropRate = skillDropRateService.findSkillDropRateByPlayerLevel(playerLevel);
        StringTokenizer st = new StringTokenizer(skillDropRate.getDropRates(), ",");
        final List<Integer> dropRates = st.get().stream().map(Integer::parseInt).limit(16).collect(Collectors.toList());

        final List<SkillDrop> skillDrops = new ArrayList<>();
        int currentPercentage = 0;
        for (int i = 0; i < dropRates.size(); i++) {
            int rate = dropRates.get(i);
            SkillDrop skillDrop = new SkillDrop();
            skillDrop.setId(i);
            skillDrop.setFrom(currentPercentage);
            skillDrop.setTo(currentPercentage + rate);
            skillDrops.add(skillDrop);
            currentPercentage += rate;
        }

        final Random random = new Random();
        final int randValue = random.nextInt(101);
        final SkillDrop skillDrop = skillDrops.stream()
                .filter(x -> x.getFrom() <= randValue && x.getTo() >= randValue)
                .findFirst()
                .orElse(null);
        if (skillDrop == null)
            return 0;

        if (levelRequired && skillDrop.getId() == 2) {
            final Random r = new Random();
            int gMeteoProbability = r.nextInt(101);
            if (gMeteoProbability <= 26) {
                skillDrop.setId(55);
            }
        }

        if (otherPlayerBattleStateDead != null) { // if someone is dead
            // rebirth drop rate
            final int dropRate = 35;
            if (dropRate >= randValue) // override
                skillDrop.setId(4);
        }
        return skillDrop.getId();
    }
}
