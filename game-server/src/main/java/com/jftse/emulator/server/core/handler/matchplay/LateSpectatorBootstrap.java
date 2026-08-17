package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.core.constants.ServeType;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.PlayerPositionInfo;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.life.room.ServeInfo;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBasicGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomSetBossGuardiansStats;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomSetGuardianStats;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomSetGuardians;
import com.jftse.emulator.server.core.packets.matchplay.S2CGameDisplayPlayerStatsPacket;
import com.jftse.emulator.server.core.packets.matchplay.S2CGameSetNameColorAndRemoveBlackBar;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplaySetPlayerPosition;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplaySpawnBossBattle;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayTriggerGuardianServe;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayTriggerServe;
import com.jftse.emulator.server.core.utils.ServingPositionGenerator;
import com.jftse.entities.database.model.battle.GuardianBase;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.shared.packets.matchplay.SMSGStartGame;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

final class LateSpectatorBootstrap {
    private LateSpectatorBootstrap() {
    }

    static IPacket[] packetsFor(GameSession session, Room room) {
        MatchplayGame game = session.getMatchplayGame();
        List<IPacket> packets = new ArrayList<>();
        if (game instanceof MatchplayGuardianGame guardianGame) {
            addGuardians(packets, guardianGame);
        }
        packets.add(SMSGStartGame.builder().result((char) 0).build());
        packets.add(new S2CGameDisplayPlayerStatsPacket(new ArrayList<>(room.getRoomPlayerList()), room.getMode()));
        packets.add(new S2CGameSetNameColorAndRemoveBlackBar(room));

        if (game instanceof MatchplayBasicGame basicGame) {
            packets.add(new Packet(PacketOperations.S2CGameRemoveBlackBars));
            packets.add(new S2CMatchplayTriggerServe(serveInfo(room, basicGame)));
        } else if (game instanceof MatchplayBattleGame battleGame) {
            packets.add(new S2CMatchplaySetPlayerPosition(positionInfo(room, battleGame.getPlayerLocationsOnMap())));
            packets.add(guardianServe(battleGame.getLastGuardianServeSide().get()));
        } else if (game instanceof MatchplayGuardianGame guardianGame) {
            packets.add(guardianServe(guardianGame.getLastGuardianServeSide().get()));
        }
        return packets.toArray(IPacket[]::new);
    }

    private static List<ServeInfo> serveInfo(Room room, MatchplayBasicGame game) {
        List<ServeInfo> result = new ArrayList<>();
        for (RoomPlayer player : activePlayers(room)) {
            ServeInfo info = new ServeInfo();
            info.setPlayerPosition(player.getPosition());
            info.setPlayerStartLocation(game.getPlayerLocationsOnMap().get(player.getPosition()));
            info.setServeType(player == game.getServePlayer().get()
                    ? ServeType.ServeBall
                    : player == game.getReceiverPlayer().get() ? ServeType.ReceiveBall : ServeType.None);
            result.add(info);
        }
        return result;
    }

    private static List<PlayerPositionInfo> positionInfo(Room room, List<Point> locations) {
        List<PlayerPositionInfo> result = new ArrayList<>();
        for (RoomPlayer player : activePlayers(room)) {
            PlayerPositionInfo info = new PlayerPositionInfo();
            info.setPlayerPosition(player.getPosition());
            info.setPlayerStartLocation(locations.get(player.getPosition()));
            result.add(info);
        }
        return result;
    }

    private static List<RoomPlayer> activePlayers(Room room) {
        return room.getRoomPlayerList().stream()
                .filter(player -> player.getPosition() < 4)
                .sorted(Comparator.comparingInt(RoomPlayer::getPosition))
                .toList();
    }

    private static S2CMatchplayTriggerGuardianServe guardianServe(int side) {
        int x = ServingPositionGenerator.randomServingPositionXOffset();
        int y = ServingPositionGenerator.randomServingPositionYOffset(x);
        return new S2CMatchplayTriggerGuardianServe((byte) side, (byte) x, (byte) y);
    }

    private static void addGuardians(List<IPacket> packets, MatchplayGuardianGame game) {
        List<GuardianBase> guardians = game.getGuardianBattleStates().stream()
                .sorted(Comparator.comparingInt(state -> state.getPosition()))
                .map(state -> game.getGuardiansInStage().stream()
                        .<GuardianBase>map(mapping -> state.isBoss()
                                ? mapping.getBossGuardian()
                                : mapping.getGuardian())
                        .filter(java.util.Objects::nonNull)
                        .filter(guardian -> guardian.getId() == state.getId())
                        .findFirst()
                        .orElseGet(() -> game.getGuardiansInBossStage().stream()
                                .<GuardianBase>map(mapping -> state.isBoss()
                                        ? mapping.getBossGuardian()
                                        : mapping.getGuardian())
                                .filter(java.util.Objects::nonNull)
                                .filter(guardian -> guardian.getId() == state.getId())
                                .findFirst()
                                .orElse(null)))
                .toList();
        GuardianBase[] slots = Arrays.copyOf(guardians.toArray(GuardianBase[]::new), 3);
        List<GuardianBase> padded = Arrays.asList(slots);
        if (game.getBossBattleActive().get()) {
            packets.add(new S2CMatchplaySpawnBossBattle(slots[0], slots[1], slots[2]));
            packets.add(new S2CRoomSetBossGuardiansStats(game.getGuardianBattleStates(), padded));
        } else {
            packets.add(new S2CRoomSetGuardians(slots[0], slots[1], slots[2]));
            packets.add(new S2CRoomSetGuardianStats(game.getGuardianBattleStates(), padded));
        }
    }
}
