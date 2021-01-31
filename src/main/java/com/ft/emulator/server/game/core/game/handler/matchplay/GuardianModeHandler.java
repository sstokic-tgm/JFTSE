package com.ft.emulator.server.game.core.game.handler.matchplay;

import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.game.core.constants.GameFieldSide;
import com.ft.emulator.server.game.core.constants.PacketEventType;
import com.ft.emulator.server.game.core.constants.RoomStatus;
import com.ft.emulator.server.game.core.constants.ServeType;
import com.ft.emulator.server.game.core.matchplay.PlayerReward;
import com.ft.emulator.server.game.core.matchplay.basic.MatchplayBasicGame;
import com.ft.emulator.server.game.core.matchplay.basic.MatchplayGuardianGame;
import com.ft.emulator.server.game.core.matchplay.event.PacketEventHandler;
import com.ft.emulator.server.game.core.matchplay.room.GameSession;
import com.ft.emulator.server.game.core.matchplay.room.Room;
import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.core.matchplay.room.ServeInfo;
import com.ft.emulator.server.game.core.packet.PacketID;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class GuardianModeHandler {
    private final GameHandler gameHandler;
    private final PacketEventHandler packetEventHandler;

    public void handleGuardianModeMatchplayPointPacket(Connection connection, C2SMatchplayPointPacket matchplayPointPacket, GameSession gameSession, MatchplayGuardianGame game) {
        // IMPLEMENT
        boolean guardianMadePoint = matchplayPointPacket.getPointsTeam() == 1;
        if (guardianMadePoint) {
            S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, (byte) 0, (byte) 0);
            gameSession.getClients().forEach(x -> {
                x.getConnection().sendTCP(triggerGuardianServePacket);
            });
        } else {
            S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Players, (byte) 0, (byte) 0);
            gameSession.getClients().forEach(x -> {
                x.getConnection().sendTCP(triggerGuardianServePacket);
            });
        }
    }

    public void handleStartGuardianMode(Room room) {
        List<ServeInfo> serveInfo = new ArrayList<>();
        List<RoomPlayer> roomPlayerList = room.getRoomPlayerList();
        List<Client> clients = this.gameHandler.getClientsInRoom(room.getRoomId());
        clients.forEach(c -> {
            RoomPlayer rp = roomPlayerList.stream()
                    .filter(x -> x.getPlayer().getId().equals(c.getActivePlayer().getId()))
                    .findFirst().orElse(null);

            GameSession gameSession = c.getActiveGameSession();
            MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();

            Point playerLocation = game.getPlayerLocationsOnMap().get(rp.getPosition());
            byte serveType = ServeType.None;
            ServeInfo playerServeInfo = new ServeInfo();
            playerServeInfo.setPlayerPosition(rp.getPosition());
            playerServeInfo.setPlayerStartLocation(playerLocation);
            playerServeInfo.setServeType(serveType);
            serveInfo.add(playerServeInfo);
        });

        S2CMatchplaySpawnBossBattle spawnBossBattle = new S2CMatchplaySpawnBossBattle((byte)0, (byte)1, (byte)0);
        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, (byte) 0, (byte) 0);
        clients.forEach(c -> {
            c.getConnection().sendTCP(spawnBossBattle);
            packetEventHandler.push(packetEventHandler.createPacketEvent(c, triggerGuardianServePacket, PacketEventType.FIRE_DELAYED, 15), PacketEventHandler.ServerClient.SERVER);
        });
    }
}
