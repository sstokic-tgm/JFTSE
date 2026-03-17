package com.jftse.emulator.server.core.manager;

import com.jftse.emulator.common.scripting.ScriptManagerFactory;
import com.jftse.emulator.common.scripting.ScriptManagerV2;
import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.constants.MiscConstants;
import com.jftse.emulator.server.core.constants.RoomPositionState;
import com.jftse.emulator.server.core.constants.RoomType;
import com.jftse.emulator.server.core.life.event.GameEventBus;
import com.jftse.emulator.server.core.life.event.GameEventType;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.guardian.PhaseManager;
import com.jftse.emulator.server.core.packets.lobby.S2CLobbyUserListAnswerPacket;
import com.jftse.emulator.server.core.packets.lobby.room.*;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.Uptime;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.*;
import com.jftse.server.core.BuildInfoProperties;
import com.jftse.server.core.ServerLoop;
import com.jftse.server.core.ServerLoopHandler;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.ServerLoopMetricsService;
import com.jftse.server.core.shared.ServerConfService;
import com.jftse.server.core.shared.ServerMetricsContext;
import com.jftse.server.core.shared.packets.SMSGInitHandshake;
import com.jftse.server.core.shared.packets.SMSGServerNotice;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomChangePosition;
import com.jftse.server.core.thread.ThreadManager;
import com.jftse.server.core.util.GameTime;
import com.jftse.server.core.util.IntervalTimer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Getter
@Setter
@Log4j2
public class GameManager implements ServerLoopHandler {
    private static GameManager instance;

    private static final Logger scriptLogger = LogManager.getLogger("ScriptLogger");

    @Autowired
    private GameSessionManager gameSessionManager;
    @Autowired
    private EventHandler eventHandler;
    @Autowired
    private ServiceManager serviceManager;
    @Autowired
    private RProducerService rProducerService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ThreadManager threadManager;

    @Autowired
    private BuildInfoProperties revisionInfo;
    @Autowired
    private ServerConfService serverConfService;

    @Autowired
    private ServerLoopMetricsService serverLoopMetrics;

    private ConcurrentLinkedQueue<FTConnection> addConnectionQueue;
    private ConcurrentLinkedDeque<FTClient> clients;
    private ConcurrentLinkedDeque<Room> rooms;
    private Room townSquare;

    private ConcurrentHashMap<Integer, String> personalBoardMessages;

    private Optional<ScriptManagerV2> scriptManager;

    private Random rnd;

    private String motd;

    private AtomicInteger playerCount = new AtomicInteger(0);
    private int maxPlayerCount = 0;

    private IntervalTimer[] timers = new IntervalTimer[ServerTimers.COUNT];

    @PostConstruct
    public void init() {
        instance = this;

        rnd = new Random();
        clients = new ConcurrentLinkedDeque<>();
        rooms = new ConcurrentLinkedDeque<>();
        personalBoardMessages = new ConcurrentHashMap<>();
        addConnectionQueue = new ConcurrentLinkedQueue<>();

        scriptManager = ScriptManagerFactory.loadScriptsV2("scripts", () -> scriptLogger);
        BattleUtils.loadStatConfig();

        //setupChatLobby();

        GameTime.updateGameTimers();
        initTimers();

        Uptime uptime = new Uptime();
        uptime.setServerType(ServerType.GAME_SERVER);
        uptime.setStartTime(GameTime.getStartTime().getEpochSecond());
        uptime.setUptime(0L);
        uptime.setRevision(revisionInfo.getFullVersion());
        serviceManager.getUptimeService().save(uptime);

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public void onExit() {
        log.info("Closing all connections");

        for (FTClient client : clients) {
            ScheduledFuture<?> sf = client.getConnection().getTimeSyncTask();
            if (sf != null) {
                sf.cancel(true);
            }

            client.getConnection().close();
        }

        rooms.clear();
        clients.clear();

        serviceManager.getUptimeService().updateUptimeAndMaxPlayers(GameTime.getUptimeSeconds(), getMaxPlayerCount(), ServerType.GAME_SERVER, GameTime.getStartTime().getEpochSecond());

        log.info("GameManager stopped");
    }

    @Override
    public void update(long diff) {
        GameTime.updateGameTimers();

        // update different timers
        for (IntervalTimer timer : timers) {
            if (timer.getCurrent() >= 0)
                timer.update(diff);
            else
                timer.setCurrent(0);
        }

        try {
            eventHandler.handleQueuedEvents();
        } catch (Exception ex) {
            log.error("Exception while handling queued events: {}", ex.getMessage(), ex);
        }

        updatePhaseManagers(diff);

        GameEventBus.call(GameEventType.ON_TICK, diff);
        updateSessions(diff);

        if (timers[ServerTimers.SUPDATE_METRICS.value()].passed()) {
            timers[ServerTimers.SUPDATE_METRICS.value()].reset();

            ServerMetricsContext ctx = ServerMetricsContext
                    .of(ServerType.GAME_SERVER, revisionInfo, ServerLoop.getInstance())
                    .attr("connections", clients.size());
            serverLoopMetrics.publishMetrics(ctx);
        }

        if (timers[ServerTimers.SUPDATE_UPTIME.value()].passed()) {
            long uptimeSeconds = GameTime.getUptimeSeconds();
            int maxOnlinePlayers = getMaxPlayerCount();
            timers[ServerTimers.SUPDATE_UPTIME.value()].reset();

            serviceManager.getUptimeService().updateUptimeAndMaxPlayers(uptimeSeconds, maxOnlinePlayers, ServerType.GAME_SERVER, GameTime.getStartTime().getEpochSecond());
        }
    }

    public static GameManager getInstance() {
        return instance;
    }

    public String getServer() {
        return ServerType.GAME_SERVER.getName();
    }

    public void addClient(FTClient client) {
        clients.add(client);
        playerCount.getAndIncrement();
        maxPlayerCount = Math.max(maxPlayerCount, playerCount.get());
    }

    public void removeClient(FTClient client) {
        clients.remove(client);
        playerCount.getAndDecrement();
    }

    public void queueConnection(FTConnection connection) {
        addConnectionQueue.offer(connection);
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
    }

    public List<FTPlayer> getPlayersInLobby() {
        return clients.stream()
                .filter(FTClient::isInLobby)
                .map(FTClient::getPlayer)
                .collect(Collectors.toList());
    }

    public List<FTClient> getClientsInLobby() {
        return clients.stream()
                .filter(FTClient::isInLobby)
                .collect(Collectors.toList());
    }

    public List<FTClient> getClientsInRoom(short roomId) {
        return clients.stream()
                .filter(c -> c.getActiveRoom() != null && c.getActiveRoom().getRoomId() == roomId)
                .collect(Collectors.toList());
    }

    public final FTConnection getConnectionByPlayerId(Long playerId) {
        return clients.stream()
                .filter(c -> c.hasPlayer() && playerId.equals(c.getPlayer().getId()))
                .findFirst()
                .map(FTClient::getConnection)
                .orElse(null);
    }

    private void setupChatLobby() {
        Room square = new Room();
        square.setRoomId(getRoomId());
        square.setRoomName("Town Square");
        square.setRoomType((byte) 1);
        square.setMode((byte) 2);
        square.setMap((byte) 0);
        square.setRule((byte) 0);
        square.setPlayers((byte) 100);
        square.setPrivate(false);
        square.setSkillFree(false);
        square.setQuickSlot(true);
        square.setLevel((byte) 0);
        square.setLevelRange((byte) 0);
        square.setBall((byte) 0);

        townSquare = square;
        addRoom(townSquare);
    }

    public synchronized void handleChatLobbyJoin(FTClient client) {
        FTConnection connection = client.getConnection();
        if (connection == null || !client.hasPlayer()) {
            return;
        }

        if (!client.getIsJoiningOrLeavingRoom().compareAndSet(false, true)) {
            return;
        }

        FTPlayer player = client.getPlayer();

        S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) 0, townSquare.getRoomType(), townSquare.getMode(), townSquare.getMap());
        connection.sendTCP(roomJoinAnswerPacket);

        final ConcurrentLinkedDeque<RoomPlayer> roomPlayerList = townSquare.getRoomPlayerList();
        boolean anyPositionAvailable = roomPlayerList.size() < townSquare.getPlayers();
        if (!anyPositionAvailable) {
            roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            client.getIsJoiningOrLeavingLobby().set(false);

            updateRoomForAllClientsInMultiplayer(connection, townSquare);
            return;
        }

        if (client.getActiveRoom() != null) {
            client.getIsJoiningOrLeavingLobby().set(false);
            return;
        }

        List<Short> positions = roomPlayerList.stream().map(RoomPlayer::getPosition).toList();
        final short position = (short) IntStream.range(0, townSquare.getPlayers())
                .filter(p -> !positions.contains((short) p))
                .findFirst()
                .orElse(-1);

        if (position == -1) {
            roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            client.getIsJoiningOrLeavingLobby().set(false);

            updateRoomForAllClientsInMultiplayer(connection, townSquare);
            return;
        }

        Friend couple = serviceManager.getSocialService().getRelationshipWithFriend(player.getPlayerRef());
        if (couple != null) {
            player.setCoupleId(couple.getFriend().getId());
            player.setCoupleName(couple.getFriend().getName());
        }

        RoomPlayer roomPlayer = new RoomPlayer(player);
        roomPlayer.setGameMaster(client.isGameMaster());
        roomPlayer.setPosition(position);
        roomPlayer.setMaster(false);
        roomPlayer.setFitting(false);

        client.setActiveRoom(townSquare);
        client.setInLobby(true);

        townSquare.getRoomPlayerList().add(roomPlayer);

        S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(townSquare);
        connection.sendTCP(roomInformationPacket);

        float spawnX = rnd.nextFloat(40.0f, 46.0f);
        float spawnY = rnd.nextFloat(60.0f, 64.0f);
        roomPlayer.setLastX(spawnX);
        roomPlayer.setLastY(spawnY);
        roomPlayer.setLastMapLayer(0);

        for (final RoomPlayer rp : townSquare.getRoomPlayerList()) {
            S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(rp, rp.getLastX(), rp.getLastY(), 0.0f, 0.0f, rp.getLastMapLayer());
            sendPacketToAllClientsInSameRoom(roomPlayerInformationPacket, connection);
        }

        Packet enableMovement = new Packet(PacketOperations.S2CEnableTownSquareMovement);
        connection.sendTCP(enableMovement);

        updateLobbyRoomListForAllClients(connection);
        refreshLobbyPlayerListForAllClients();

        client.getIsJoiningOrLeavingRoom().set(false);
    }

    public void refreshLobbyPlayerListForAllClients() {
        final List<FTClient> clientsInLobby = getClientsInLobby();
        clientsInLobby.forEach(c -> {
            if (c.getConnection() != null) {
                final int currentPage = c.getLobbyCurrentPlayerListPage();
                final List<FTPlayer> playersInLobby = getPlayersInLobby().stream()
                        .skip(currentPage == 1 ? 0 : (currentPage * 10L) - 10)
                        .limit(10)
                        .collect(Collectors.toList());
                S2CLobbyUserListAnswerPacket lobbyUserListAnswerPacket = new S2CLobbyUserListAnswerPacket(playersInLobby);
                c.getConnection().sendTCP(lobbyUserListAnswerPacket);
            }
        });
    }

    public synchronized void handleRoomPlayerChanges(final FTConnection connection, final boolean notifyClients) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        FTPlayer activePlayer = client.getPlayer();

        Room room = client.getActiveRoom();
        if (room == null)
            return;

        final boolean isTownSquare = room.getRoomType() == 1 && room.getMode() == 2;
        final ConcurrentLinkedDeque<RoomPlayer> roomPlayerList = room.getRoomPlayerList();
        final Optional<RoomPlayer> roomPlayer = Optional.ofNullable(client.getRoomPlayer());

        final short playerPosition = roomPlayer.isPresent() ? roomPlayer.get().getPosition() : -1;

        final boolean isMaster = roomPlayer.isPresent() && roomPlayer.get().isMaster();
        if (isMaster) {
            long slotCount = roomPlayerList.stream().filter(rp -> !rp.isMaster() && rp.getPosition() < 4).count();
            if (slotCount > 0) {
                roomPlayerList.stream()
                        .filter(rp -> !rp.isMaster() && rp.getPosition() < 4)
                        .findFirst()
                        .ifPresent(rp -> {
                            rp.setMaster(true);
                            rp.setReady(false);
                        });
            } else {
                roomPlayerList.stream()
                        .filter(rp -> !rp.isMaster())
                        .findFirst()
                        .ifPresent(rp -> {
                            rp.setMaster(true);
                            rp.setReady(false);
                        });
            }
        }

        if (isMaster) {
            roomPlayer.get().setMaster(false);
            roomPlayer.get().setReady(false);
        }

        if (!isTownSquare) {
            if (playerPosition == 9) {
                room.getPositions().set(playerPosition, RoomPositionState.Locked);
            } else if (playerPosition != -1) {
                room.getPositions().set(playerPosition, RoomPositionState.Free);
            }
        }

        roomPlayerList.removeIf(rp -> rp.getPlayerId() == activePlayer.getId());
        if (room.getRoomPlayerList().isEmpty() && !isTownSquare) {
            removeRoom(room);
        }

        if (!isTownSquare) {
            final GameSession activeGameSession = client.getActiveGameSession();
            if (activeGameSession == null) {
                S2CLeaveRoomWithPositionPacket leaveRoomWithPositionPacket = new S2CLeaveRoomWithPositionPacket(playerPosition);
                GameManager.getInstance().sendPacketToAllClientsInSameRoom(leaveRoomWithPositionPacket, connection);
            } else {
                GameSession gameSession = gameSessionManager.getGameSessionBySessionId(client.getGameSessionId());
                if (gameSession != null) {
                    gameSession.getClients().removeIf(c -> c.hasPlayer() && c.getPlayer().getId() == activePlayer.getId());
                }
                client.setActiveGameSession(null);
            }
        }

        if (playerPosition != -1) {
            if (notifyClients) {
                S2CLeaveRoomWithPositionPacket leaveRoomWithPositionPacket = new S2CLeaveRoomWithPositionPacket(playerPosition);
                getClientsInRoom(room.getRoomId()).forEach(c -> {
                    if (c.hasPlayer() && c.getConnection() != null) {
                        c.getConnection().sendTCP(leaveRoomWithPositionPacket);
                    }
                });
                updateRoomForAllClientsInMultiplayer(connection, room);
            }
        }

        if (isMaster) {
            RoomPlayer newMaster = roomPlayerList.stream().filter(RoomPlayer::isMaster).findFirst().orElse(null);
            if (newMaster != null) {
                S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(newMaster, 0.0f, 0.0f, 0.0f, 0.0f, 0);
                S2CRoomFittingPlayerInfoPacket roomFittingPlayerInfoPacket = new S2CRoomFittingPlayerInfoPacket(newMaster.getPosition(), newMaster);
                SMSGRoomChangePosition roomPositionChangeAnswerPacket = SMSGRoomChangePosition.builder().result((char) 0).oldPosition(newMaster.getPosition()).newPosition(newMaster.getPosition()).build();
                getClientsInRoom(room.getRoomId()).forEach(c -> {
                    if (c.getConnection() != null && c.getPlayer().getId() != activePlayer.getId()) {
                        c.getConnection().sendTCP(roomFittingPlayerInfoPacket);
                        c.getConnection().sendTCP(roomPlayerInformationPacket);
                        c.getConnection().sendTCP(roomPositionChangeAnswerPacket);
                    }
                });
            }
        }

        client.setActiveRoom(null);
    }

    public void updateRoomForAllClientsInMultiplayer(final FTConnection connection, final Room room) {
        FTClient client = connection.getClient();
        final boolean isTownSquare = room.getRoomType() == 1 && room.getMode() == 2;

        short roomPlayerPosition = -1;
        final RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer != null) {
            roomPlayerPosition = roomPlayer.getPosition();
        }
        boolean shouldUpdateNonGM = !isTownSquare && (roomPlayerPosition != MiscConstants.InvisibleGmSlot || !client.isGameMaster());

        S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
        getClientsInRoom(room.getRoomId()).forEach(c -> {
            if (c.getConnection() != null && shouldUpdateNonGM) {
                c.getConnection().sendTCP(roomInformationPacket);
            }
        });
        updateLobbyRoomListForAllClients(connection);
    }

    public void updateLobbyRoomListForAllClients(final FTConnection connection) {
        getClientsInLobby().forEach(c -> {
            if (c.getConnection() != null && c.getConnection().getId() != connection.getId()) {
                S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(getFilteredRoomsForClient(c));
                c.getConnection().sendTCP(roomListAnswerPacket);
            }
        });
    }

    public List<Room> getFilteredRoomsForClient(FTClient client) {
        final int clientRoomModeFilter = client.getLobbyGameModeTabFilter();
        final int currentRoomListPage = Math.max(client.getLobbyCurrentRoomListPage(), 0);
        return getRooms().stream()
                .filter(r -> (clientRoomModeFilter == GameMode.ALL || getRoomMode(r) == clientRoomModeFilter) && !(r.getRoomType() == 1 && r.getMode() == 2))
                .skip(currentRoomListPage * 5L)
                .limit(5)
                .collect(Collectors.toList());
    }

    public int getRoomMode(final Room room) {
        if (room.getRoomType() == RoomType.BATTLEMON) {
            return GameMode.BATTLEMON;
        }
        return room.getMode();
    }

    public synchronized void internalHandleRoomCreate(final FTConnection connection, Room room) {
        room.getPositions().set(0, RoomPositionState.InUse);

        byte players = room.getPlayers();
        if (players == 2) {
            room.getPositions().set(2, RoomPositionState.Locked);
            room.getPositions().set(3, RoomPositionState.Locked);
        }

        FTClient client = connection.getClient();
        FTPlayer activePlayer = client.getPlayer();

        Friend couple = serviceManager.getSocialService().getRelationshipWithFriend(activePlayer.getPlayerRef());
        if (couple != null) {
            activePlayer.setCoupleId(couple.getFriend().getId());
            activePlayer.setCoupleName(couple.getFriend().getName());
        }

        RoomPlayer roomPlayer = new RoomPlayer(activePlayer);
        roomPlayer.setGameMaster(client.isGameMaster());
        roomPlayer.setPosition((short) 0);
        roomPlayer.setMaster(true);
        roomPlayer.setFitting(false);

        client.setActiveRoom(room);
        client.setInLobby(false);

        room.getRoomPlayerList().add(roomPlayer);
        addRoom(room);

        S2CRoomCreateAnswerPacket roomCreateAnswerPacket = new S2CRoomCreateAnswerPacket((char) 0, room.getRoomType(), room.getMode(), room.getMap());
        S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
        S2CRoomPlayerListInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerListInformationPacket(new ArrayList<>(room.getRoomPlayerList()));

        connection.sendTCP(roomCreateAnswerPacket);
        connection.sendTCP(roomInformationPacket);
        connection.sendTCP(roomPlayerInformationPacket);

        updateLobbyRoomListForAllClients(connection);
        refreshLobbyPlayerListForAllClients();
    }

    public synchronized short getRoomId() {
        List<Short> roomIds = getRooms().stream().map(Room::getRoomId).sorted().collect(Collectors.toList());
        short currentRoomId = 0;
        for (Short roomId : roomIds) {
            if (roomId != currentRoomId) {
                return currentRoomId;
            }
            currentRoomId++;
        }
        return currentRoomId;
    }

    public GuildMember getGuildMemberByPlayerPositionInGuild(int playerPositionInGuild, final GuildMember guildMember) {
        Guild guild = serviceManager.getGuildService().findWithMembersById(guildMember.getGuild().getId());
        return getGuildMemberByPlayerPositionInGuild(guild, playerPositionInGuild);
    }

    public GuildMember getGuildMemberByPlayerPositionInGuild(Guild guild, int playerPositionInGuild) {
        final List<GuildMember> memberList = guild.getMemberList().stream()
                .filter(x -> !x.getWaitingForApproval())
                .sorted(Comparator.comparing(GuildMember::getMemberRank).reversed())
                .toList();
        if (memberList.size() < playerPositionInGuild) {
            return null;
        }

        return memberList.get(playerPositionInGuild - 1);
    }

    public boolean isAllowedToChangeMode(Room room) {
        List<RoomPlayer> activePlayingPlayers = room.getRoomPlayerList().stream().filter(x -> x.getPosition() < 4).toList();
        return activePlayingPlayers.stream().allMatch(x -> x.getLevel() >= configService.getValue("command.room.mode.change.player.level", 60));
    }

    public void sendPacketToAllClientsInSameGameSession(IPacket packet, FTConnection connection) {
        final GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession != null) {
            final ArrayList<FTClient> clientsInGameSession = new ArrayList<>(gameSession.getClients());
            clientsInGameSession.forEach(c -> {
                if (c.getConnection() != null) {
                    c.getConnection().sendTCP(packet);
                }
            });
        }
    }

    public void sendPacketToAllClientsInSameRoom(IPacket packet, FTConnection connection) {
        final Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            final List<FTClient> clientsInRoom = new ArrayList<>(getClientsInRoom(room.getRoomId()));
            clientsInRoom.forEach(c -> {
                if (c.getConnection() != null) {
                    c.getConnection().sendTCP(packet);
                }
            });
        }
    }

    private void updatePhaseManagers(long diff) {
        final ConcurrentHashMap<Integer, GameSession> gameSessions = gameSessionManager.getGameSessionList();

        for (GameSession gameSession : gameSessions.values()) {
            if (!gameSession.isValid()) {
                continue;
            }

            if (gameSession.isGuardianMode()) {
                final MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getMatchplayGame();
                final PhaseManager phaseManager = game.getPhaseManager();
                if (phaseManager != null) {
                    phaseManager.update(diff);
                }
            }
        }
    }

    private void updateSessions(long diff) {
        FTConnection conn;
        while ((conn = addConnectionQueue.poll()) != null) {
            initializeConnection(conn);
        }

        for (FTClient client : clients) {
            FTConnection connection = client.getConnection();
            if (connection == null || !connection.update(diff)) {
                removeClient(client);
            }
        }
    }

    private void initializeConnection(FTConnection conn) {
        if (conn.getClient() != null) {
            log.warn("({}) Connection already has a client assigned", conn.getIPString());
            return;
        }

        InetSocketAddress inetSocketAddress = conn.getRemoteAddressTCP();
        String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";

        FTClient client = new FTClient();
        client.setIp(remoteAddress.substring(1, remoteAddress.lastIndexOf(":")));
        client.setPort(Integer.parseInt(remoteAddress.substring(remoteAddress.indexOf(":") + 1)));
        client.setConnection(conn);
        conn.setClient(client);
        addClient(client);

        SMSGInitHandshake initHandshakePacket = SMSGInitHandshake.builder()
                .decKey(conn.getDecryptionKey())
                .encKey(conn.getEncryptionKey())
                .decTblIdx(0)
                .encTblIdx(0)
                .build();
        conn.sendTCP(initHandshakePacket);

        final String motd = GameManager.getInstance().getMotd();
        SMSGServerNotice serverNotice = SMSGServerNotice.builder().message(motd).build();
        if (!StringUtils.isEmpty(motd)) {
            ThreadManager.getInstance().schedule(() -> conn.sendTCP(serverNotice), 100, TimeUnit.MILLISECONDS);
        }
    }

    private void initTimers() {
        for (int i = 0; i < ServerTimers.COUNT; i++) {
            timers[i] = new IntervalTimer();
        }
        timers[ServerTimers.SUPDATE_UPTIME.value()].setInterval(TimeUnit.MINUTES.toMillis(serverConfService.get("UpdateUptimeInterval", Integer.class)));
        timers[ServerTimers.SUPDATE_METRICS.value()].setInterval(TimeUnit.SECONDS.toMillis(serverConfService.get("UpdateMetricsInterval", Integer.class)));
    }

    public enum ServerTimers {
        SUPDATE_METRICS,
        SUPDATE_UPTIME;

        public static final int COUNT = values().length;

        public int value() {
            return this.ordinal();
        }
    }
}
