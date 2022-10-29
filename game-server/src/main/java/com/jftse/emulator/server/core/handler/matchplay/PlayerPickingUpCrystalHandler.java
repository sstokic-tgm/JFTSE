package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.common.utilities.StringTokenizer;
import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.packets.matchplay.C2SMatchplayPlayerPicksUpCrystal;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayGiveSpecificSkill;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.event.RunnableEventHandler;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.task.PlaceCrystalRandomlyTask;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.server.core.matchplay.battle.SkillCrystal;
import com.jftse.server.core.matchplay.battle.SkillDrop;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.battle.SkillDropRate;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.SkillDropRateService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@PacketOperationIdentifier(PacketOperations.C2SMatchplayPlayerPicksUpCrystal)
public class PlayerPickingUpCrystalHandler extends AbstractPacketHandler {
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
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getActiveGameSession() == null
                || ftClient.getActiveRoom() == null || ftClient.getPlayer() == null)
            return;

        final Player player = ftClient.getPlayer();
        if (player == null)
            return;

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
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(packet, ftClient.getConnection());

            if (isBattleGame) {
                ((MatchplayBattleGame) game).getSkillCrystals().removeIf(sc -> sc.getId() == skillCrystal.getId());
            } else {
                ((MatchplayGuardianGame) game).getSkillCrystals().removeIf(sc -> sc.getId() == skillCrystal.getId());
            }

            PlaceCrystalRandomlyTask placeCrystalRandomlyTask = isBattleGame ? new PlaceCrystalRandomlyTask(ftClient.getConnection(), gameFieldSide) : new PlaceCrystalRandomlyTask(ftClient.getConnection());
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
