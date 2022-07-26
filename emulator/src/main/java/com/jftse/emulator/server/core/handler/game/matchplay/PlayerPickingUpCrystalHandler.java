package com.jftse.emulator.server.core.handler.game.matchplay;

import com.jftse.emulator.common.utilities.StringTokenizer;
import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.core.matchplay.battle.SkillDrop;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.battle.SkillCrystal;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.event.RunnableEventHandler;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.matchplay.C2SMatchplayPlayerPicksUpCrystal;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayGiveSpecificSkill;
import com.jftse.emulator.server.core.service.SkillDropRateService;
import com.jftse.emulator.server.core.task.PlaceCrystalRandomlyTask;
import com.jftse.entities.database.model.battle.SkillDropRate;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class PlayerPickingUpCrystalHandler extends AbstractHandler {
    private C2SMatchplayPlayerPicksUpCrystal playerPicksUpCrystalPacket;

    private final SkillDropRateService skillDropRateService;

    private final RunnableEventHandler runnableEventHandler;

    public PlayerPickingUpCrystalHandler() {
        skillDropRateService = ServiceManager.getInstance().getSkillDropRateService();

        runnableEventHandler = GameManager.getInstance().getRunnableEventHandler();
    }

    @Override
    public boolean process(Packet packet) {
        playerPicksUpCrystalPacket = new C2SMatchplayPlayerPicksUpCrystal(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActiveGameSession() == null
                || connection.getClient().getActiveRoom() == null || connection.getClient().getPlayer() == null)
            return;

        final Player player = connection.getClient().getPlayer();
        if (player == null)
            return;

        final RoomPlayer roomPlayer = connection.getClient().getRoomPlayer();
        if (roomPlayer == null)
            return;

        final Room activeRoom = connection.getClient().getActiveRoom();
        if (activeRoom == null)
            return;

        short playerPosition = roomPlayer.getPosition();
        final GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession == null)
            return;

        final MatchplayGame game = gameSession.getActiveMatchplayGame();
        if (game == null)
            return;

        boolean isBattleGame = game instanceof MatchplayBattleGame;

        SkillCrystal skillCrystal = isBattleGame ?
                ((MatchplayBattleGame) game).getSkillCrystals().stream()
                        .filter(x -> x.getId() == playerPicksUpCrystalPacket.getCrystalId())
                        .findFirst()
                        .orElse(null) :
                ((MatchplayGuardianGame) game).getSkillCrystals().stream()
                        .filter(x -> x.getId() == playerPicksUpCrystalPacket.getCrystalId())
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
                    .filter(rp -> rp.getPlayer() != null)
                    .allMatch(p -> p.getPlayer().getLevel() == 65);

            int randomSkillIndex = this.getRandomPlayerSkill(player, playerBattleState, levelRequired);
            S2CMatchplayGiveSpecificSkill packet = new S2CMatchplayGiveSpecificSkill(playerPicksUpCrystalPacket.getCrystalId(), playerPosition, randomSkillIndex);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(packet, connection);

            if (isBattleGame) {
                new ArrayList<>(((MatchplayBattleGame) game).getSkillCrystals()).forEach(current -> {
                    if (current.getId() == skillCrystal.getId()) {
                        ((MatchplayBattleGame) game).getSkillCrystals().remove(current);
                        return;
                    }
                });
            } else {
                new ArrayList<>(((MatchplayGuardianGame) game).getSkillCrystals()).forEach(current -> {
                    if (current.getId() == skillCrystal.getId()) {
                        ((MatchplayGuardianGame) game).getSkillCrystals().remove(current);
                        return;
                    }
                });
            }

            PlaceCrystalRandomlyTask placeCrystalRandomlyTask = isBattleGame ? new PlaceCrystalRandomlyTask(connection, gameFieldSide) : new PlaceCrystalRandomlyTask(connection);
            long crystalSpawnInterval = isBattleGame ? ((MatchplayBattleGame) game).getCrystalSpawnInterval() : ((MatchplayGuardianGame) game).getCrystalSpawnInterval();

            RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(placeCrystalRandomlyTask, crystalSpawnInterval);
            gameSession.getRunnableEvents().add(runnableEvent);
        }
    }

    private int getRandomPlayerSkill(Player player, PlayerBattleState otherPlayerBattleStateDead, boolean levelRequired) {
        final SkillDropRate skillDropRate = skillDropRateService.findSkillDropRateByPlayer(player);
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
