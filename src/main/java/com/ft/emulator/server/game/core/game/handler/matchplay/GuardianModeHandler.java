package com.ft.emulator.server.game.core.game.handler.matchplay;

import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.game.core.constants.GameFieldSide;
import com.ft.emulator.server.game.core.constants.PacketEventType;
import com.ft.emulator.server.game.core.constants.RoomStatus;
import com.ft.emulator.server.game.core.constants.ServeType;
import com.ft.emulator.server.game.core.matchplay.PlayerHealth;
import com.ft.emulator.server.game.core.matchplay.PlayerReward;
import com.ft.emulator.server.game.core.matchplay.basic.MatchplayBasicGame;
import com.ft.emulator.server.game.core.matchplay.basic.MatchplayGuardianGame;
import com.ft.emulator.server.game.core.matchplay.event.PacketEventHandler;
import com.ft.emulator.server.game.core.matchplay.room.GameSession;
import com.ft.emulator.server.game.core.matchplay.room.Room;
import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.core.matchplay.room.ServeInfo;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.game.core.packet.packets.lobby.room.S2CRoomSetGuardians;
import com.ft.emulator.server.game.core.packet.packets.matchplay.*;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import com.ft.emulator.server.shared.module.GameHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.awt.*;
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

    public void handleGuardianModeMatchplayPointPacket(Connection connection, C2SMatchplayPointPacket matchplayPointPacket, GameSession gameSession, MatchplayGuardianGame game) {
        boolean guardianMadePoint = matchplayPointPacket.getPointsTeam() == 1;
        // TODO: If player makes the point, guardian take dmg dependent on players will

        if (guardianMadePoint) {
            // TODO: Get player to damage (if guardian attacks dmg nearest player to net)
            gameSession.getClients().forEach(c -> {
                RoomPlayer roomPlayer = c.getActiveRoom().getRoomPlayerList().stream()
                        .filter(x -> x.getPlayer().getId() == c.getActivePlayer().getId())
                        .findFirst()
                        .orElse(null);
                PlayerHealth playerHealth = game.getPlayerHPs().get(roomPlayer.getPosition());
                short lossBallDamage = (short) (playerHealth.getMaxPlayerHealth() * 0.1);
                short newPlayerHealth = game.damagePlayer(roomPlayer.getPosition(), lossBallDamage);
                S2CMatchplayDamageToPlayer damageToPlayerPacket = new S2CMatchplayDamageToPlayer(roomPlayer.getPosition(), newPlayerHealth);
                c.getConnection().sendTCP(damageToPlayerPacket);
            });
        } else{
            // TODO: Deal 10% of max health to guardians whe guardians loose ball
            List<Short> guardianPositions = Arrays.asList((short) 10, (short) 11, (short) 12);
            short newGuardianHealth = -1;
            S2CMatchplayDamageToPlayer damageToPlayerPacket = new S2CMatchplayDamageToPlayer(guardianPositions.get(0), newGuardianHealth);
            connection.sendTCP(damageToPlayerPacket);
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
}
