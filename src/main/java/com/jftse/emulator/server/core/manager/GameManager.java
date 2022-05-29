package com.jftse.emulator.server.core.manager;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.constants.GameMode;
import com.jftse.emulator.server.core.constants.RoomPositionState;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.event.PacketEventHandler;
import com.jftse.emulator.server.core.matchplay.event.RunnableEventHandler;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.lobby.S2CLobbyUserListAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.*;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.messenger.Friend;
import com.jftse.emulator.server.database.model.player.ClothEquipment;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
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
    private PacketEventHandler packetEventHandler;
    @Autowired
    private RunnableEventHandler runnableEventHandler;
    @Autowired
    private ServiceManager serviceManager;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ThreadManager threadManager;

    private boolean running;

    private ConcurrentLinkedDeque<Client> clients;
    private ConcurrentLinkedDeque<Room> rooms;

    @PostConstruct
    public void init() {
        instance = this;

        running = true;
        setupGlobalTasks();

        clients = new ConcurrentLinkedDeque<>();
        rooms = new ConcurrentLinkedDeque<>();

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static GameManager getInstance() {
        return instance;
    }

    public void addClient(Client client) {
        clients.add(client);
    }

    public void removeClient(Client client) {
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
                .filter(Client::isInLobby)
                .map(Client::getPlayer)
                .collect(Collectors.toList());
    }

    public List<Client> getClientsInLobby() {
        return clients.stream()
                .filter(Client::isInLobby)
                .collect(Collectors.toList());
    }

    public List<Client> getClientsInRoom(short roomId) {
        return clients.stream()
                .filter(c -> c.getActiveRoom() != null && c.getActiveRoom().getRoomId() == roomId)
                .collect(Collectors.toList());
    }

    public final Connection getConnectionByPlayerId(Long playerId) {
        return clients.stream()
                .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(playerId))
                .findFirst()
                .map(Client::getConnection)
                .orElse(null);
    }

    private void setupGlobalTasks() {
        threadManager.newTask(() -> {
            log.info("Queued packet handling started");
            while (running) {
                try {
                    packetEventHandler.handleQueuedPackets();

                    Thread.sleep(10);
                } catch (InterruptedException ie) {
                    log.error(ie.getMessage(), ie);
                } catch (Exception ex) {
                    log.error(String.format("Exception in runnable thread: %s", ex.getMessage()), ex);
                }
            }
        });
        threadManager.newTask(() -> {
            log.info("Queued runnable event handling started");
            while (running) {
                try {
                    List<GameSession> gameSessions = new ArrayList<>(gameSessionManager.getGameSessionList());
                    gameSessions.forEach(gameSession -> {
                        if (gameSession != null)
                            runnableEventHandler.handleQueuedRunnableEvents(gameSession);
                    });

                    Thread.sleep(10);
                } catch (InterruptedException ie) {
                    log.error(ie.getMessage(), ie);
                } catch (Exception ex) {
                    log.error(String.format("Exception in runnable thread: %s", ex.getMessage()), ex);
                }
            }
        });
    }

    @PreDestroy
    public void onExit() {
        running = false;
    }

    public void refreshLobbyPlayerListForAllClients() {
        final List<Client> clientsInLobby = getClientsInLobby();
        clientsInLobby.forEach(c -> {
            if (c.getConnection() != null && c.getConnection().isConnected()) {
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

    public synchronized void handleRoomPlayerChanges(final Connection connection, final boolean notifyClients) {
        Client client = connection.getClient();
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
        final boolean isMaster = roomPlayer.isPresent() && roomPlayer.get().isMaster();
        if (isMaster) {
            long slotCount = roomPlayerList.stream().filter(rp -> !rp.isMaster() && rp.getPosition() < 4).count();
            if (slotCount > 0) {
                roomPlayerList.stream()
                        .filter(rp -> !rp.isMaster() && rp.getPosition() < 4)
                        .findFirst()
                        .ifPresent(rp -> {
                            synchronized (rp) {
                                rp.setMaster(true);
                                rp.setReady(false);
                            }
                        });
            } else {
                roomPlayerList.stream()
                        .filter(rp -> !rp.isMaster())
                        .findFirst()
                        .ifPresent(rp -> {
                            synchronized (rp) {
                                rp.setMaster(true);
                                rp.setReady(false);
                            }
                        });
            }
        }

        if (playerPosition == 9) {
            room.getPositions().set(playerPosition, RoomPositionState.Locked);
        } else {
            room.getPositions().set(playerPosition, RoomPositionState.Free);
        }

        roomPlayerList.removeIf(rp -> rp.getPlayerId().equals(activePlayer.getId()));
        if (room.getRoomPlayerList().isEmpty()) {
            removeRoom(room);
        }

        final GameSession activeGameSession = client.getActiveGameSession();
        if (activeGameSession == null) {
            S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(new ArrayList<>(roomPlayerList));
            S2CRoomPositionChangeAnswerPacket roomPositionChangeAnswerPacket = new S2CRoomPositionChangeAnswerPacket((char) 0, playerPosition, (short) 9);
            getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (notifyClients) {
                    if (c.getPlayer() != null && !c.getPlayer().getId().equals(activePlayer.getId()) && c.getConnection() != null && c.getConnection().isConnected()) {
                        c.getConnection().sendTCP(roomPlayerInformationPacket, roomPositionChangeAnswerPacket);
                    }
                }
            });
        } else {
            GameSession gameSession = gameSessionManager.getGameSessionBySessionId(activeGameSession.getSessionId());
            if (gameSession != null) {
                gameSession.getClients().removeIf(c -> c.getPlayer() != null && c.getPlayer().getId().equals(activePlayer.getId()));
            }
            client.setActiveGameSession(null);
        }
        client.setActiveRoom(null);
        if (notifyClients) {
            S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(new ArrayList<>(roomPlayerList));
            getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null && c.getConnection().isConnected()) {
                    c.getConnection().sendTCP(roomPlayerInformationPacket);
                }
            });
            updateRoomForAllClientsInMultiplayer(connection, room);
        }
    }

    public void updateRoomForAllClientsInMultiplayer(final Connection connection, final Room room) {
        S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
        getClientsInRoom(room.getRoomId()).forEach(c -> {
            if (c.getConnection() != null && c.getConnection().isConnected()) {
                c.getConnection().sendTCP(roomInformationPacket);
            }
        });
        updateLobbyRoomListForAllClients(connection);
    }

    public void updateLobbyRoomListForAllClients(final Connection connection) {
        getClientsInLobby().forEach(c -> {
            if (c.getConnection() != null && c.getConnection().isConnected() && c.getConnection().getId() != connection.getId()) {
                S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(getFilteredRoomsForClient(c));
                c.getConnection().sendTCP(roomListAnswerPacket);
            }
        });
    }

    public List<Room> getFilteredRoomsForClient(Client client) {
        final int clientRoomModeFilter = client.getLobbyGameModeTabFilter();
        final int currentRoomListPage = Math.max(client.getLobbyCurrentRoomListPage(), 0);
        return getRooms().stream()
                .filter(r -> clientRoomModeFilter == GameMode.ALL || getRoomMode(r) == clientRoomModeFilter)
                .skip(currentRoomListPage * 5L)
                .limit(5)
                .collect(Collectors.toList());
    }

    public int getRoomMode(final Room room) {
        if (room.getAllowBattlemon() == 2) {
            return GameMode.BATTLEMON;
        }
        return room.getMode();
    }

    public synchronized void internalHandleRoomCreate(final Connection connection, Room room) {
        room.getPositions().set(0, RoomPositionState.InUse);
        room.setAllowBattlemon((byte) 0);

        byte players = room.getPlayers();
        if (players == 2) {
            room.getPositions().set(2, RoomPositionState.Locked);
            room.getPositions().set(3, RoomPositionState.Locked);
        }

        Player activePlayer = connection.getClient().getPlayer();

        RoomPlayer roomPlayer = new RoomPlayer();
        roomPlayer.setPlayerId(activePlayer.getId());

        GuildMember guildMember = serviceManager.getGuildMemberService().getByPlayer(activePlayer);
        Friend couple = serviceManager.getSocialService().getRelationship(activePlayer);
        ClothEquipment clothEquipment = serviceManager.getClothEquipmentService().findClothEquipmentById(roomPlayer.getPlayer().getClothEquipment().getId());
        StatusPointsAddedDto statusPointsAddedDto = serviceManager.getClothEquipmentService().getStatusPointsFromCloths(roomPlayer.getPlayer());

        roomPlayer.setGuildMemberId(guildMember == null ? null : guildMember.getId());
        roomPlayer.setCoupleId(couple == null ? null : couple.getId());
        roomPlayer.setClothEquipmentId(clothEquipment.getId());
        roomPlayer.setStatusPointsAddedDto(statusPointsAddedDto);
        roomPlayer.setPosition((short) 0);
        roomPlayer.setMaster(true);
        roomPlayer.setFitting(false);
        room.getRoomPlayerList().add(roomPlayer);

        addRoom(room);
        connection.getClient().setActiveRoom(room);
        connection.getClient().setInLobby(false);

        S2CRoomCreateAnswerPacket roomCreateAnswerPacket = new S2CRoomCreateAnswerPacket((char) 0, (byte) 0, (byte) 0, (byte) 0);
        S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
        S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(new ArrayList<>(room.getRoomPlayerList()));

        connection.sendTCP(roomCreateAnswerPacket, roomInformationPacket);
        connection.sendTCP(roomPlayerInformationPacket);

        updateLobbyRoomListForAllClients(connection);
        refreshLobbyPlayerListForAllClients();

        List<Packet> roomSlotCloseAnswerPackets = new ArrayList<>();
        // TODO: Temporarily. Delete these lines if spectators work
        for (int i = 5; i < 9; i++) {
            connection.getClient().getActiveRoom().getPositions().set(i, RoomPositionState.Locked);
            S2CRoomSlotCloseAnswerPacket roomSlotCloseAnswerPacket = new S2CRoomSlotCloseAnswerPacket((byte) i, true);
            roomSlotCloseAnswerPackets.add(roomSlotCloseAnswerPacket);
        }

        getClientsInRoom(room.getRoomId()).forEach(c -> {
            if (c.getConnection() != null && c.getConnection().isConnected()) {
                c.getConnection().sendTCP(roomSlotCloseAnswerPackets.toArray(Packet[]::new));
            }
        });
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
        final List<GuildMember> memberList = guildMember.getGuild()
                .getMemberList()
                .stream().filter(x -> !x.getWaitingForApproval())
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

    public void sendPacketToAllClientsInSameGameSession(Packet packet, Connection connection) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession != null) {
            synchronized (gameSession) {
                final ArrayList<Client> clientsInGameSession = new ArrayList<>(gameSession.getClients());
                clientsInGameSession.forEach(c -> {
                    if (c.getConnection() != null && c.getConnection().isConnected()) {
                        c.getConnection().sendTCP(packet);
                    }
                });
            }
        }
    }

    public void sendPacketToAllRelayClientsInSameGameSession(Packet packet, Connection connection) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession != null) {
            synchronized (gameSession) {
                final ArrayList<Client> clientsInGameSession = new ArrayList<>(gameSession.getClientsInRelay());
                clientsInGameSession.forEach(c -> {
                    if (c.getConnection() != null && c.getConnection().isConnected()) {
                        c.getConnection().sendTCP(packet);
                    }
                });
            }
        }
    }
}
