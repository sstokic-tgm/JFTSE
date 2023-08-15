package com.jftse.emulator.server.core.manager;

import com.google.protobuf.Empty;
import com.jftse.emulator.common.scripting.ScriptManager;
import com.jftse.emulator.common.scripting.ScriptManagerFactory;
import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.constants.ChatMode;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.packets.lobby.S2CLobbyUserListAnswerPacket;
import com.jftse.emulator.server.core.packets.lobby.room.*;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.*;
import com.jftse.proto.auth.ClientServiceGrpc;
import com.jftse.proto.auth.FTClientList;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.thread.ThreadManager;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.netty.channel.ChannelFutureListener;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@Getter
@Setter
@Log4j2
public class GameManager {
    private static GameManager instance;

    @Autowired
    private GameSessionManager gameSessionManager;
    @Autowired
    private EventHandler eventHandler;
    @Autowired
    private ServiceManager serviceManager;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ThreadManager threadManager;

    private AtomicBoolean running;
    private ConcurrentLinkedDeque<FTClient> clients;
    private ConcurrentLinkedDeque<Room> rooms;

    private Future<?> eventHandlerTask;
    private Future<?> loggedInClientTask;

    private ManagedChannel rpcChannelAuthServer;

    private Optional<ScriptManager> scriptManager;

    @PostConstruct
    public void init() {
        instance = this;

        rpcChannelAuthServer = ManagedChannelBuilder.forTarget(configService.getValue("rpc.auth-server.url", "localhost:9898"))
                .usePlaintext()
                .enableRetry()
                .maxRetryAttempts(20)
                .build();

        clients = new ConcurrentLinkedDeque<>();
        rooms = new ConcurrentLinkedDeque<>();

        scriptManager = ScriptManagerFactory.loadScripts("scripts", () -> log);

        running = new AtomicBoolean(true);

        setupGlobalTasks();

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public void onExit() {
        if (running.compareAndSet(true, false)) {
            if (eventHandlerTask != null && eventHandlerTask.cancel(false))
                log.info("EventHandlerTask stopped");
            if (loggedInClientTask != null && loggedInClientTask.cancel(false))
                log.info("LoggedInClientTask stopped");

            log.info("Closing all connections");
            for (FTClient client : clients) {
                client.getConnection().close();
            }
            log.info("All connections closed");

            rooms.clear();
            clients.clear();

            log.info("Closing RPC channel");
            if (rpcChannelAuthServer != null && !rpcChannelAuthServer.isShutdown())
                rpcChannelAuthServer.shutdownNow();

            log.info("GameManager stopped");
        }
    }

    public static GameManager getInstance() {
        return instance;
    }

    public void addClient(FTClient client) {
        clients.add(client);
    }

    public void removeClient(FTClient client) {
        clients.remove(client);
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
    }

    public List<Player> getPlayersInLobby() {
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
                .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(playerId))
                .findFirst()
                .map(FTClient::getConnection)
                .orElse(null);
    }

    private void setupGlobalTasks() {
        eventHandlerTask = threadManager.submit(() -> {
            while (running.get()) {
                try {
                    eventHandler.handleQueuedEvents();
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (Exception ex) {
                    log.error(String.format("Exception in runnable thread: %s", ex.getMessage()), ex);
                }
            }
            log.info("EventHandlerTask stopped");
        });
        log.info("EventHandlerTask started");

        loggedInClientTask = threadManager.scheduleAtFixedRate(() -> {
            log.info("Checking for logged in clients...");
            try {
                if (running.get()) {
                    final List<Account> loggedInAccounts = serviceManager.getAuthenticationService().findByStatus((int) AuthenticationServiceImpl.ACCOUNT_ALREADY_LOGGED_IN);
                    final List<FTClient> clients = new ArrayList<>(this.clients);

                    ConnectivityState state = rpcChannelAuthServer.getState(true);
                    if (state != ConnectivityState.READY) {
                        log.info("[rpc] Reconnecting to auth server...");
                        rpcChannelAuthServer = ManagedChannelBuilder.forTarget(configService.getValue("rpc.auth-server.url", "localhost:9898"))
                                .usePlaintext()
                                .enableRetry()
                                .maxRetryAttempts(20)
                                .build();
                    }

                    ClientServiceGrpc.ClientServiceBlockingStub stub = ClientServiceGrpc.newBlockingStub(rpcChannelAuthServer);
                    log.info("[rpc] Requesting online client list from auth server...");
                    FTClientList clientList = stub.getClientList(Empty.getDefaultInstance());
                    log.info("[rpc] Received online client list from auth server with size {}", clientList.getClientCount());

                    final List<com.jftse.proto.auth.FTClient> clientsFromAuthServer = clientList.getClientList();

                    clientsFromAuthServer.forEach(c -> {
                        final boolean isAlreadyLoggedInAuthServer = loggedInAccounts.removeIf(a -> a.getId() == c.getAccountId());
                        if (!isAlreadyLoggedInAuthServer) {
                            FTClient client = new FTClient();
                            client.setAccountId(c.getAccountId());
                            clients.add(client);
                        }
                    });

                    if (loggedInAccounts.isEmpty()) {
                        log.info("No stuck logged in accounts found in game server");
                        return;
                    }

                    for (Account account : loggedInAccounts) {
                        final Optional<FTClient> client = clients.stream()
                                .filter(c -> c.getAccountId() != null && c.getAccountId().equals(account.getId()))
                                .findFirst();
                        if (client.isEmpty()) {
                            resetLoginStatusByAccount(account);
                        } else {
                            final FTClient ftClient = client.get();
                            final FTConnection ftConnection = ftClient.getConnection();
                            if (ftConnection == null) {
                                resetLoginStatusByAccount(account);

                                removeClient(ftClient);
                                continue;
                            }

                            ftConnection.sendTCP(new Packet(PacketOperations.C2SHeartbeat)).addListener((ChannelFutureListener) future -> {
                                if (!future.isSuccess()) {
                                    resetLoginStatusByAccount(account);

                                    future.channel().close();
                                    removeClient(ftClient);
                                }
                            });
                        }
                    }
                    log.info("Checking for logged in clients finished");
                }
            } catch (Exception ex) {
                log.error(String.format("Exception in runnable thread: %s", ex.getMessage()), ex);
            }
        }, 100, 40, TimeUnit.SECONDS);
    }

    private void resetLoginStatusByAccount(Account account) {
        log.info("Account {} is not connected to the server anymore, setting status to {}", account.getUsername(), AuthenticationServiceImpl.SUCCESS);
        account.setStatus((int) AuthenticationServiceImpl.SUCCESS);
        serviceManager.getAuthenticationService().updateAccount(account);

        final List<Player> players = serviceManager.getPlayerService().findAllByAccount(account);
        players.stream()
                .filter(Player::getOnline)
                .forEach(p -> {
                    p.setOnline(false);
                    serviceManager.getPlayerService().save(p);
                });
    }

    public void refreshLobbyPlayerListForAllClients() {
        final List<FTClient> clientsInLobby = getClientsInLobby();
        clientsInLobby.forEach(c -> {
            if (c.getConnection() != null) {
                final int currentPage = c.getLobbyCurrentPlayerListPage();
                final List<Player> playersInLobby = getPlayersInLobby().stream()
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
        if (client == null)
            return;

        Player activePlayer = connection.getClient().getPlayer();
        if (activePlayer == null)
            return;

        Room room = client.getActiveRoom();
        if (room == null)
            return;

        ConcurrentLinkedDeque<RoomPlayer> roomPlayerList = room.getRoomPlayerList();
        final Optional<RoomPlayer> roomPlayer = Optional.ofNullable(client.getRoomPlayer());

        final short playerPosition = roomPlayer.isPresent() ? roomPlayer.get().getPosition() : -1;

        roomPlayerList.removeIf(rp -> rp.getPlayerId().equals(activePlayer.getId()));
        if (room.getRoomPlayerList().isEmpty()) {
            removeRoom(room);
        } else {
            List<Short> positions = roomPlayerList.stream().map(RoomPlayer::getPosition).collect(Collectors.toList());
            int nextPosition = room.getNextPlayerPosition().getAndUpdate(currentPosition -> {
                if (currentPosition == playerPosition) {
                    return currentPosition;
                } else if (currentPosition >= 0 && currentPosition < room.getPlayers() && !positions.contains((short) currentPosition)) {
                    return currentPosition;
                } else {
                    return -1;
                }
            });

            if (nextPosition != -1) {
                while (positions.contains((short) nextPosition)) {
                    nextPosition = (nextPosition + 1) % room.getPlayers();
                }
                room.getNextPlayerPosition().set(nextPosition);
            }
        }

        if (playerPosition != -1) {
            if (notifyClients) {
                S2CLeaveRoomWithPositionPacket leaveRoomWithPositionPacket = new S2CLeaveRoomWithPositionPacket(playerPosition);
                getClientsInRoom(room.getRoomId()).forEach(c -> {
                    if (c.getPlayer() != null && !c.getPlayer().getId().equals(activePlayer.getId()) && c.getConnection() != null) {
                        c.getConnection().sendTCP(leaveRoomWithPositionPacket);
                    }
                });
                updateRoomForAllClientsInMultiplayer(connection, room);
            }
        }
        client.setActiveRoom(null);

        if (roomPlayer.isPresent()) {
            RoomPlayer rp = roomPlayer.get();
            if (rp.isMaster()) {
                Packet roomLeaveAnswer = new Packet(PacketOperations.S2CRoomLeaveAnswer);
                roomLeaveAnswer.write(0);

                getClientsInRoom(room.getRoomId()).forEach(c -> {
                    FTConnection ftConnection = c.getConnection();
                    RoomPlayer cRP = c.getRoomPlayer();
                    if (cRP != null && ftConnection != null) {
                        S2CLeaveRoomWithPositionPacket leaveRoomWithPositionPacket = new S2CLeaveRoomWithPositionPacket(cRP.getPosition());
                        ftConnection.sendTCP(leaveRoomWithPositionPacket);
                        ftConnection.sendTCP(roomLeaveAnswer);
                    }
                    c.setActiveRoom(null);
                });
                removeRoom(room);
                updateLobbyRoomListForAllClients(connection);
            }
        }
    }

    public void updateRoomForAllClientsInMultiplayer(final FTConnection connection, final Room room) {
        S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
        getClientsInRoom(room.getRoomId()).forEach(c -> {
            if (c.getConnection() != null) {
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
                .filter(r -> clientRoomModeFilter == ChatMode.ALL || getChatMode(r) == clientRoomModeFilter)
                .skip(currentRoomListPage * 5L)
                .limit(5)
                .collect(Collectors.toList());
    }

    public int getChatMode(final Room room) {
        return switch (room.getMode()) {
            case 0 -> ChatMode.CHAT;
            case 1 -> ChatMode.MY_HOME;
            default -> ChatMode.ALL;
        };
    }

    public synchronized void internalHandleRoomCreate(final FTConnection connection, Room room) {
        final int nextPosition = room.getNextPlayerPosition().getAndIncrement();

        room.setAllowBattlemon((byte) 0);

        Player activePlayer = connection.getClient().getPlayer();

        RoomPlayer roomPlayer = new RoomPlayer();
        roomPlayer.setPlayerId(activePlayer.getId());

        GuildMember guildMember = serviceManager.getGuildMemberService().getByPlayer(activePlayer);
        Friend couple = serviceManager.getSocialService().getRelationship(activePlayer);
        ClothEquipment clothEquipment = serviceManager.getClothEquipmentService().findClothEquipmentById(roomPlayer.getPlayer().getClothEquipment().getId());
        SpecialSlotEquipment specialSlotEquipment = serviceManager.getSpecialSlotEquipmentService().findById(roomPlayer.getPlayer().getSpecialSlotEquipment().getId());
        CardSlotEquipment cardSlotEquipment = serviceManager.getCardSlotEquipmentService().findById(roomPlayer.getPlayer().getCardSlotEquipment().getId());
        StatusPointsAddedDto statusPointsAddedDto = serviceManager.getClothEquipmentService().getStatusPointsFromCloths(roomPlayer.getPlayer());

        roomPlayer.setGuildMemberId(guildMember == null ? null : guildMember.getId());
        roomPlayer.setCoupleId(couple == null ? null : couple.getId());
        roomPlayer.setClothEquipmentId(clothEquipment.getId());
        roomPlayer.setSpecialSlotEquipmentId(specialSlotEquipment.getId());
        roomPlayer.setCardSlotEquipmentId(cardSlotEquipment.getId());
        roomPlayer.setStatusPointsAddedDto(statusPointsAddedDto);
        roomPlayer.setPosition((short) nextPosition);
        roomPlayer.setMaster(true);
        roomPlayer.setFitting(false);
        room.getRoomPlayerList().add(roomPlayer);

        addRoom(room);
        connection.getClient().setActiveRoom(room);
        connection.getClient().setInLobby(false);

        S2CRoomCreateAnswerPacket roomCreateAnswerPacket = new S2CRoomCreateAnswerPacket((char) 0, room.getRoomType(), room.getMode(), room.getMap());
        S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
        S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(roomPlayer);

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
        final List<GuildMember> memberList = guildMember.getGuild().getMemberList().stream()
                .filter(x -> !x.getWaitingForApproval())
                .sorted(Comparator.comparing(GuildMember::getMemberRank).reversed())
                .collect(Collectors.toList());
        if (memberList.size() < playerPositionInGuild) {
            return null;
        }

        return memberList.get(playerPositionInGuild - 1);
    }

    public boolean isAllowedToChangeMode(Room room) {
        List<RoomPlayer> activePlayingPlayers = room.getRoomPlayerList().stream().filter(x -> x.getPosition() < 4).collect(Collectors.toList());
        return activePlayingPlayers.stream().allMatch(x -> x.getPlayer().getLevel() >= configService.getValue("command.room.mode.change.player.level", 60));
    }

    public void sendPacketToAllClientsInSameGameSession(Packet packet, FTConnection connection) {
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

    public void sendPacketToAllClientsInSameRoom(Packet packet, FTConnection connection) {
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
}
