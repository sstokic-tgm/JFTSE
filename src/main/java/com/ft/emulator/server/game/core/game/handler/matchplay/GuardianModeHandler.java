package com.ft.emulator.server.game.core.game.handler.matchplay;

import com.ft.emulator.server.database.model.battle.Skill;
import com.ft.emulator.server.database.model.battle.SkillDropRate;
import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.game.core.constants.GameFieldSide;
import com.ft.emulator.server.game.core.constants.PacketEventType;
import com.ft.emulator.server.game.core.matchplay.battle.GuardianBattleState;
import com.ft.emulator.server.game.core.matchplay.battle.PlayerBattleState;
import com.ft.emulator.server.game.core.matchplay.basic.MatchplayGuardianGame;
import com.ft.emulator.server.game.core.matchplay.battle.SkillCrystal;
import com.ft.emulator.server.game.core.matchplay.battle.SkillDrop;
import com.ft.emulator.server.game.core.matchplay.event.PacketEventHandler;
import com.ft.emulator.server.game.core.matchplay.event.RunnableEvent;
import com.ft.emulator.server.game.core.matchplay.event.RunnableEventHandler;
import com.ft.emulator.server.game.core.matchplay.room.GameSession;
import com.ft.emulator.server.game.core.matchplay.room.Room;
import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.core.packet.packets.lobby.room.S2CRoomSetGuardianStats;
import com.ft.emulator.server.game.core.packet.packets.lobby.room.S2CRoomSetGuardians;
import com.ft.emulator.server.game.core.packet.packets.matchplay.*;
import com.ft.emulator.server.game.core.service.SkillDropRateService;
import com.ft.emulator.server.game.core.service.SkillService;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import com.ft.emulator.server.shared.module.GameHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class GuardianModeHandler {
    private final static long crystalDefaultRespawnTime = TimeUnit.SECONDS.toMillis(5);
    private final static long crystalDefaultDespawnTime = TimeUnit.SECONDS.toMillis(5);

    private final GameHandler gameHandler;
    private final PacketEventHandler packetEventHandler;
    private final RunnableEventHandler runnableEventHandler;
    private final SkillService skillService;
    private final SkillDropRateService skillDropRateService;
    // HUGE BIG TODO: Clean up/Cancel all pending runnables when game ends. They interfer with a new running gamesession of same id

    public void handleGuardianModeMatchplayPointPacket(Connection connection, C2SMatchplayPointPacket matchplayPointPacket, GameSession gameSession, MatchplayGuardianGame game) {
        boolean guardianMadePoint = matchplayPointPacket.getPointsTeam() == 1;
        // TODO: If player makes the point, guardian take dmg dependent on players will

        if (guardianMadePoint) {
            List<Packet> dmgPackets = new ArrayList<>();
            gameSession.getClients().forEach(c -> {
                RoomPlayer roomPlayer = c.getActiveRoom().getRoomPlayerList().stream()
                        .filter(x -> x.getPlayer().getId() == c.getActivePlayer().getId())
                        .findFirst()
                        .orElse(null);
                PlayerBattleState playerBattleState = game.getPlayerBattleStates().get(roomPlayer.getPosition());
                short lossBallDamage = (short) -(playerBattleState.getMaxHealth() * 0.1);
                short newPlayerHealth = game.damagePlayer(roomPlayer.getPosition(), lossBallDamage);
                S2CMatchplayDealDamage damageToPlayerPacket = new S2CMatchplayDealDamage(roomPlayer.getPosition(), newPlayerHealth, (byte) 0, 0, 0);
                dmgPackets.add(damageToPlayerPacket);
            });
            this.sendPacketsToAllClientsInSameGameSession(dmgPackets, connection);
        } else {
            List<Short> guardianPositions = Arrays.asList((short) 10, (short) 11, (short) 12);
            guardianPositions.forEach(x -> {
                GuardianBattleState guardianBattleState = game.getGuardianBattleStates().get(x);
                if (guardianBattleState == null) return;
                short lossBallDamage = (short) -(guardianBattleState.getMaxHealth() * 0.02);
                short newGuardianHealth = game.damageGuardian(x, lossBallDamage);
                S2CMatchplayDealDamage damageToGuardianPacket = new S2CMatchplayDealDamage(x, newGuardianHealth, (byte) 0, 0, 0);
                this.sendPacketToAllClientsInSameGameSession(damageToGuardianPacket, connection);
            });
        }

        boolean lastGuardianServeWasOnGuardianSide = game.getLastGuardianServeSide() == GameFieldSide.Guardian;
        if (!lastGuardianServeWasOnGuardianSide) {
            game.setLastGuardianServeSide(GameFieldSide.Guardian);
            S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, (byte) 0, (byte) 0);
            gameSession.getClients().forEach(x -> {
                x.getConnection().sendTCP(triggerGuardianServePacket);
            });
        } else {
            game.setLastGuardianServeSide(GameFieldSide.Players);
            S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Players, (byte) 0, (byte) 0);
            gameSession.getClients().forEach(x -> {
                x.getConnection().sendTCP(triggerGuardianServePacket);
            });
        }
    }

    public void handleStartGuardianMode(Connection connection, Room room) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();
        game.setLastGuardianServeSide(GameFieldSide.Guardian);
        List<Client> clients = this.gameHandler.getClientsInRoom(room.getRoomId());
        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, (byte) 0, (byte) 0);
        clients.forEach(c -> {
            packetEventHandler.push(packetEventHandler.createPacketEvent(c, triggerGuardianServePacket, PacketEventType.FIRE_DELAYED, 8), PacketEventHandler.ServerClient.SERVER);
        });

        this.placeCrystalRandomly(connection, game);
        this.triggerGuardianAttackLoop(connection);
    }

    private void triggerGuardianAttackLoop(Connection connection) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession == null) return;

        List<Integer> guardianSkills = Arrays.asList(2, 3, 5, 6, 7, 10, 11, 12);
        byte randIndex = (byte) (Math.random() * guardianSkills.size());
        byte skillId = guardianSkills.get(randIndex).byteValue();
        Point2D point = new Point2D.Float();

        // Crab, Small inferno, Big inferno
        if (skillId == 11 || skillId == 27 || skillId == 35) point = this.getRandomPoint();

        Point2D finalPoint = point;
        RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(() -> {
            S2CMatchplayUseSkill packet = new S2CMatchplayUseSkill((byte) 10, (byte) 0, skillId, finalPoint);
            this.sendPacketToAllClientsInSameGameSession(packet, connection);
            this.triggerGuardianAttackLoop(connection);
        }, TimeUnit.SECONDS.toMillis(7));
        gameSession.getRunnableEvents().add(runnableEvent);
    }

    public void handlePrepareGuardianMode(Connection connection, Room room) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();

        // TODO: Store HP for each player and guardian correctly
        short defaultGuardianHealth = 500;

        List<RoomPlayer> roomPlayers = room.getRoomPlayerList();
        List<PlayerBattleState> playerBattleStates = roomPlayers.stream().filter(x -> x.getPosition() < 4).map(rp -> {
            Player player = rp.getPlayer();
            short baseHp = (short) (200 + (3 * (player.getLevel() - 1)));
            PlayerBattleState playerBattleState = new PlayerBattleState();
            playerBattleState.setCurrentHealth(baseHp);
            playerBattleState.setMaxHealth(baseHp);
            return playerBattleState;
        }).collect(Collectors.toList());
        game.setPlayerBattleStates(playerBattleStates);

        byte guardianStartPosition = 10;
        List<Byte> guardians = Arrays.asList((byte) 1, (byte) 0, (byte) 0);
        HashMap<Short, GuardianBattleState> guardianBattleStates = new HashMap<>();
        for (int i = 0; i < guardians.stream().count(); i++) {
            GuardianBattleState guardianBattleState = new GuardianBattleState();
            guardianBattleState.setCurrentHealth(defaultGuardianHealth);
            guardianBattleState.setMaxHealth(defaultGuardianHealth);
            guardianBattleStates.put((short) (i + guardianStartPosition), guardianBattleState);
        }
        game.setGuardianBattleStates(guardianBattleStates);

        byte amountOfGuardians = (byte) guardians.stream().filter(x -> x > 0).count();
        S2CRoomSetGuardians roomSetGuardians = new S2CRoomSetGuardians(guardians.get(0), guardians.get(1), guardians.get(2));
        S2CRoomSetGuardianStats roomSetGuardianStats = new S2CRoomSetGuardianStats(amountOfGuardians, defaultGuardianHealth);
        List<Client> clients = this.gameHandler.getClientsInRoom(room.getRoomId());
        clients.forEach(c -> {
            c.getConnection().sendTCP(roomSetGuardians);
            c.getConnection().sendTCP(roomSetGuardianStats);
        });
    }

    public void handlePlayerPickingUpCrystal(Connection connection, C2SMatchplayPlayerPicksUpCrystal playerPicksUpCrystalPacket) {
        RoomPlayer roomPlayer = this.getRoomPlayerFromConnection(connection);
        short playerPosition = roomPlayer.getPosition();
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();
        SkillCrystal skillCrystal = game.getSkillCrystals().stream()
                .filter(x -> x.getId() == playerPicksUpCrystalPacket.getCrystalId())
                .findFirst()
                .orElse(null);

        if (skillCrystal != null) {
            if (gameSession == null) return;
            S2CMatchplayLetCrystalDisappear letCrystalDisappearPacket = new S2CMatchplayLetCrystalDisappear(skillCrystal.getId());
            this.sendPacketToAllClientsInSameGameSession(letCrystalDisappearPacket, connection);

            int skillId = this.getRandomSkillBasedOnProbability(roomPlayer.getPlayer());
            List<Short> playerSkills = game.assignSkillToPlayer(playerPosition, (short) skillId);
            S2CMatchplayGivePlayerSkills givePlayerSkillsPacket = new S2CMatchplayGivePlayerSkills(playerPosition, playerSkills.get(0), playerSkills.get(1));
            this.sendPacketToAllClientsInSameGameSession(givePlayerSkillsPacket, connection);
            game.getSkillCrystals().remove(skillCrystal);
            RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(() -> this.placeCrystalRandomly(connection, game), this.crystalDefaultRespawnTime);
            gameSession.getRunnableEvents().add(runnableEvent);
        }
    }

    public void handlePlayerUseSkill(Connection connection, C2SMatchplayUsesSkill playerUseSkill) {
        byte playerPos = playerUseSkill.getPlayerPosition();
        if (playerPos > 3) return;

        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();
        List<Short> playerSkills = game.removeSkillFromTopOfStackFromPlayer(playerUseSkill.getPlayerPosition());
        S2CMatchplayGivePlayerSkills givePlayerSkillsPacket =
                new S2CMatchplayGivePlayerSkills(playerUseSkill.getPlayerPosition(), playerSkills.get(0), playerSkills.get(1));
        this.sendPacketToAllClientsInSameGameSession(givePlayerSkillsPacket, connection);
    }

    public void handleSkillHitsTarget(Connection connection, C2SMatchplaySkillHitsTarget skillHitsTarget) {
        byte skillId = skillHitsTarget.getSkillId();

        // Lets ignore ball damage here for now
        if (skillId == 0 || skillHitsTarget.getDamageType() == 1) {
            return;
        }

        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();
        Skill skill = skillService.findSkillById((long)skillId);
        short skillDamage = skill.getDamage().shortValue();

        short targetPosition = skillHitsTarget.getTargetPosition();
        short newHealth;
        if (targetPosition < 4) {
            if (skillDamage > 1) {
                newHealth = game.healPlayer(targetPosition, skillDamage);
            } else {
                newHealth = game.damagePlayer(targetPosition, skillDamage);
            }
        } else {
            newHealth = game.damageGuardian(targetPosition, skillDamage);
        }

        S2CMatchplayDealDamage damageToPlayerPacket =
                new S2CMatchplayDealDamage(targetPosition, newHealth, skillId, skillHitsTarget.getXKnockbackPosition(), skillHitsTarget.getYKnockbackPosition());
        this.sendPacketToAllClientsInSameGameSession(damageToPlayerPacket, connection);
    }

    private void placeCrystalRandomly(Connection connection, MatchplayGuardianGame game) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession == null) return;
        Point2D point = this.getRandomPoint();

        short crystalId = (short) (game.getLastCrystalId() + 1);
        game.setLastCrystalId(crystalId);
        SkillCrystal skillCrystal = new SkillCrystal();
        skillCrystal.setId(crystalId);
        game.getSkillCrystals().add(skillCrystal);

        S2CMatchplayPlaceSkillCrystal placeSkillCrystal = new S2CMatchplayPlaceSkillCrystal(skillCrystal.getId(), point);
        this.sendPacketToAllClientsInSameGameSession(placeSkillCrystal, connection);

        Runnable despawnCrystalRunnable = () -> {
            if (gameSession == null) return;
            boolean isCrystalStillAvailable = game.getSkillCrystals().stream().anyMatch(x -> x.getId() == skillCrystal.getId());
            if (isCrystalStillAvailable) {
                S2CMatchplayLetCrystalDisappear letCrystalDisappearPacket = new S2CMatchplayLetCrystalDisappear(skillCrystal.getId());
                this.sendPacketToAllClientsInSameGameSession(letCrystalDisappearPacket, connection);
                game.getSkillCrystals().remove(skillCrystal);
                RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(() -> this.placeCrystalRandomly(connection, game), this.crystalDefaultRespawnTime);
                gameSession.getRunnableEvents().add(runnableEvent);
            }
        };

        RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(despawnCrystalRunnable, this.crystalDefaultDespawnTime);
        gameSession.getRunnableEvents().add(runnableEvent);
    }

    private Point2D getRandomPoint() {
        int negator = (int) (Math.random() * 2) == 0 ? -1 : 1;
        float xPos = (float) (Math.random() * 60) * negator;
        float yPos = (short) (Math.random() * 120) * -1;
        yPos = Math.abs(yPos) < 5 ? -5 : yPos;
        return new Point2D.Float(xPos, yPos);
    }

    public int getRandomSkillBasedOnProbability(Player player) {
        SkillDropRate skillDropRate = skillDropRateService.findSkillDropRateByPlayer(player);
        String dropRates = skillDropRate.getDropRates();
        List<Integer> dropRatesInt = Arrays.stream(dropRates.split(",")).map(x -> Integer.parseInt(x)).collect(Collectors.toList());

        List<SkillDrop> skillDrops = new ArrayList<>();
        int currentPercentage = 0;
        for (int i = 0; i < dropRatesInt.size(); i++) {
            int item = dropRatesInt.get(i);
            if (item != 0) {
                SkillDrop skillDrop = new SkillDrop();
                skillDrop.setId(i);
                skillDrop.setFrom(currentPercentage);
                skillDrop.setTo(currentPercentage + item);
                skillDrops.add(skillDrop);
                currentPercentage += item;
            }
        }

        Random random = new Random();
        int randValue = random.nextInt(101);
        SkillDrop skillDrop = skillDrops.stream().filter(x -> x.getFrom() <= randValue && x.getTo() >= randValue).findFirst().orElse(null);
        return skillDrop.getId();
    }

    private RoomPlayer getRoomPlayerFromConnection(Connection connection) {
        RoomPlayer roomPlayer = connection.getClient().getActiveRoom().getRoomPlayerList().stream()
                .filter(x -> x.getPlayer().getId() == connection.getClient().getActivePlayer().getId())
                .findFirst()
                .orElse(null);
        return roomPlayer;
    }

    private void sendPacketToAllClientsInSameGameSession(Packet packet, Connection connection) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        gameSession.getClients().forEach(c -> c.getConnection().sendTCP(packet));
    }

    private void sendPacketsToAllClientsInSameGameSession(List<Packet> packets, Connection connection) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        gameSession.getClients().forEach(c -> {
            packets.forEach(p -> c.getConnection().sendTCP(p));
        });
    }
}
