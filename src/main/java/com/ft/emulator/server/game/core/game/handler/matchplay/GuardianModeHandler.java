package com.ft.emulator.server.game.core.game.handler.matchplay;

import com.ft.emulator.server.game.core.constants.GameFieldSide;
import com.ft.emulator.server.game.core.constants.PacketEventType;
import com.ft.emulator.server.game.core.matchplay.battle.PlayerHealth;
import com.ft.emulator.server.game.core.matchplay.basic.MatchplayGuardianGame;
import com.ft.emulator.server.game.core.matchplay.battle.SkillCrystal;
import com.ft.emulator.server.game.core.matchplay.event.PacketEventHandler;
import com.ft.emulator.server.game.core.matchplay.event.RunnableEvent;
import com.ft.emulator.server.game.core.matchplay.event.RunnableEventHandler;
import com.ft.emulator.server.game.core.matchplay.room.GameSession;
import com.ft.emulator.server.game.core.matchplay.room.Room;
import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.core.packet.packets.lobby.room.S2CRoomSetGuardians;
import com.ft.emulator.server.game.core.packet.packets.matchplay.*;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import com.ft.emulator.server.shared.module.GameHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class GuardianModeHandler {
    private final GameHandler gameHandler;
    private final PacketEventHandler packetEventHandler;
    private final RunnableEventHandler runnableEventHandler;

    public void handleGuardianModeMatchplayPointPacket(Connection connection, C2SMatchplayPointPacket matchplayPointPacket, GameSession gameSession, MatchplayGuardianGame game) {
        boolean guardianMadePoint = matchplayPointPacket.getPointsTeam() == 1;
        // TODO: If player makes the point, guardian take dmg dependent on players will

        if (guardianMadePoint) {
            List<Packet> dmgPackets = new ArrayList<>();
            // TODO: Get player to damage (if guardian attacks dmg nearest player to net)
            gameSession.getClients().forEach(c -> {
                RoomPlayer roomPlayer = c.getActiveRoom().getRoomPlayerList().stream()
                        .filter(x -> x.getPlayer().getId() == c.getActivePlayer().getId())
                        .findFirst()
                        .orElse(null);
                PlayerHealth playerHealth = game.getPlayerHPs().get(roomPlayer.getPosition());
                short lossBallDamage = (short) (playerHealth.getMaxPlayerHealth() * 0.1);
                short newPlayerHealth = game.damagePlayer(roomPlayer.getPosition(), lossBallDamage);
                S2CMatchplayDealDamage damageToPlayerPacket = new S2CMatchplayDealDamage(roomPlayer.getPosition(), newPlayerHealth);
                dmgPackets.add(damageToPlayerPacket);
            });
            this.sendPacketsToAllClientsInSameGameSession(dmgPackets, connection);
        } else{
            // TODO: Deal 2% of max health to guardians when guardians loose ball
            List<Short> guardianPositions = Arrays.asList((short) 10, (short) 11, (short) 12);
            guardianPositions.forEach(x -> {
                short newGuardianHealth = -1;
                S2CMatchplayDealDamage damageToGuardianPacket = new S2CMatchplayDealDamage(x, newGuardianHealth);
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
    }

    public void handlePrepareGuardianMode(Connection connection, Room room) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();

        // TODO: Store HP for each player correctly
        short defaultPlayerHealth = 200;
        List<RoomPlayer> roomPlayers = room.getRoomPlayerList();
        List<PlayerHealth> playerHealths = roomPlayers.stream().filter(x -> x.getPosition() < 4).map(x -> {
            PlayerHealth playerHealth = new PlayerHealth();
            playerHealth.setCurrentPlayerHealth(defaultPlayerHealth);
            playerHealth.setMaxPlayerHealth(defaultPlayerHealth);
            return playerHealth;
        }).collect(Collectors.toList());
        game.setPlayerHPs(playerHealths);

        S2CRoomSetGuardians roomSetGuardians = new S2CRoomSetGuardians((byte) 1, (byte) 0, (byte) 0);
        List<Client> clients = this.gameHandler.getClientsInRoom(room.getRoomId());
        clients.forEach(c -> {
            c.getConnection().sendTCP(roomSetGuardians);
        });
    }

    public void handlePlayerPickingUpCrystal(Connection connection, C2SMatchplayPlayerPicksUpCrystal playerPicksUpCrystalPacket) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();
        SkillCrystal skillCrystal = game.getSkillCrystals().stream()
                .filter(x -> x.getId() == playerPicksUpCrystalPacket.getSkillIndex())
                .findFirst()
                .orElse(null);

        if (skillCrystal != null) {
            S2CMatchplayLetCrystalDisappear letCrystalDisappearPacket = new S2CMatchplayLetCrystalDisappear(skillCrystal.getId());
            this.sendPacketToAllClientsInSameGameSession(letCrystalDisappearPacket, connection);
            S2CMatchplayGivePlayerSkills givePlayerSkillsPacket = new S2CMatchplayGivePlayerSkills(playerPicksUpCrystalPacket.getPlayerPosition(), skillCrystal.getSkillId(), -1);
            connection.sendTCP(givePlayerSkillsPacket);
            game.getSkillCrystals().remove(skillCrystal);
            RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(() -> this.placeCrystalRandomly(connection, game), TimeUnit.SECONDS.toMillis(15));
            this.runnableEventHandler.push(runnableEvent);
        }
    }

    private void placeCrystalRandomly(Connection connection, MatchplayGuardianGame game) {
        short skillIndex = (short) (Math.random() * 14);
        int negator = (int) (Math.random() * 2) == 0 ? -1 : 1;
        float xPos = (float) (Math.random() * 40) * negator;
        float yPos = (short) (Math.random() * 120) * -1;

        short crystalId = (short) (game.getLastCrystalId() + 1);
        game.setLastCrystalId(crystalId);
        SkillCrystal skillCrystal = new SkillCrystal();
        skillCrystal.setId(crystalId);
        skillCrystal.setSkillId(skillIndex);
        game.getSkillCrystals().add(skillCrystal);

        // TODO: Store skills player have in game session and assign 1. 2. skill slot based on that.
        // TODO: When picking up 3. skill, skill on the 2nd slot gets discarded and 1nd moves to 2nd
        S2CMatchplayPlaceSkillCrystal placeSkillCrystal = new S2CMatchplayPlaceSkillCrystal(skillCrystal.getId(), xPos, yPos);
        this.sendPacketToAllClientsInSameGameSession(placeSkillCrystal, connection);

        Runnable despawnCrystalRunnable = () -> {
            boolean isCrystalStillAvailable = game.getSkillCrystals().stream().anyMatch(x -> x.getId() == skillCrystal.getId());
            if (isCrystalStillAvailable) {
                S2CMatchplayLetCrystalDisappear letCrystalDisappearPacket = new S2CMatchplayLetCrystalDisappear(skillCrystal.getId());
                this.sendPacketToAllClientsInSameGameSession(letCrystalDisappearPacket, connection);
                game.getSkillCrystals().remove(skillCrystal);
                RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(() -> this.placeCrystalRandomly(connection, game), TimeUnit.SECONDS.toMillis(15));
                this.runnableEventHandler.push(runnableEvent);
            }
        };

        RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(despawnCrystalRunnable, TimeUnit.SECONDS.toMillis(5));
        this.runnableEventHandler.push(runnableEvent);
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
