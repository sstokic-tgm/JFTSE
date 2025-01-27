package com.jftse.emulator.server.core.manager;

import com.jftse.emulator.common.scripting.ScriptManager;
import com.jftse.emulator.common.scripting.ScriptManagerFactory;
import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.constants.MiscConstants;
import com.jftse.emulator.server.core.constants.RoomPositionState;
import com.jftse.emulator.server.core.constants.RoomType;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.packets.lobby.S2CLobbyUserListAnswerPacket;
import com.jftse.emulator.server.core.packets.lobby.room.*;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.*;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.thread.ThreadManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private Room townSquare;

    private ConcurrentHashMap<Integer, String> personalBoardMessages;

    private Future<?> eventHandlerTask;

    private Optional<ScriptManager> scriptManager;

    private Random rnd;

    @PostConstruct
    public void init() {
        instance = this;

        rnd = new Random();
        clients = new ConcurrentLinkedDeque<>();
        rooms = new ConcurrentLinkedDeque<>();
        personalBoardMessages = new ConcurrentHashMap<>();

        scriptManager = ScriptManagerFactory.loadScripts("scripts", () -> log);

        running = new AtomicBoolean(true);

        //setupChatLobby();
        setupGlobalTasks();

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public void onExit() {
        if (running.compareAndSet(true, false)) {
            if (eventHandlerTask != null && eventHandlerTask.cancel(false))
                log.info("EventHandlerTask stopped");

            log.info("Closing all connections");
            for (FTClient client : clients) {
                client.getConnection().close();
            }
            log.info("All connections closed");

            rooms.clear();
            clients.clear();

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
                } catch (Exception ex) {
                    log.error(String.format("Exception in runnable thread: %s", ex.getMessage()), ex);
                }
            }
            log.info("EventHandlerTask stopped");
        });
        log.info("EventHandlerTask started");
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
        if (connection == null) {
            return;
        }

        if (!client.getIsJoiningOrLeavingRoom().compareAndSet(false, true)) {
            return;
        }

        Player player = client.getPlayer();

        S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) 0, townSquare.getRoomType(), townSquare.getMode(), townSquare.getMap());
        connection.sendTCP(roomJoinAnswerPacket);

        if (player == null) {
            client.getIsJoiningOrLeavingLobby().set(false);
            return;
        }

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

        RoomPlayer roomPlayer = new RoomPlayer();
        roomPlayer.setPlayerId(player.getId());

        GuildMember guildMember = ServiceManager.getInstance().getGuildMemberService().getByPlayer(player);
        Friend couple = ServiceManager.getInstance().getSocialService().getRelationship(player);
        ClothEquipment clothEquipment = ServiceManager.getInstance().getClothEquipmentService().findClothEquipmentById(player.getClothEquipment().getId());
        SpecialSlotEquipment specialSlotEquipment = ServiceManager.getInstance().getSpecialSlotEquipmentService().findById(player.getSpecialSlotEquipment().getId());
        CardSlotEquipment cardSlotEquipment = ServiceManager.getInstance().getCardSlotEquipmentService().findById(player.getCardSlotEquipment().getId());
        StatusPointsAddedDto statusPointsAddedDto = ServiceManager.getInstance().getClothEquipmentService().getStatusPointsFromCloths(player);

        roomPlayer.setGuildMemberId(guildMember == null ? null : guildMember.getId());
        roomPlayer.setCoupleId(couple == null ? null : couple.getId());
        roomPlayer.setClothEquipmentId(clothEquipment.getId());
        roomPlayer.setSpecialSlotEquipmentId(specialSlotEquipment.getId());
        roomPlayer.setCardSlotEquipmentId(cardSlotEquipment.getId());
        roomPlayer.setStatusPointsAddedDto(statusPointsAddedDto);
        roomPlayer.setPosition(position);
        roomPlayer.setMaster(false);
        roomPlayer.setFitting(false);

        townSquare.getRoomPlayerList().add(roomPlayer);

        client.setActiveRoom(townSquare);
        client.setInLobby(true);

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

        Player activePlayer = client.getPlayer();
        if (activePlayer == null)
            return;

        Room room = client.getActiveRoom();
        if (room == null)
            return;

        final boolean isTownSquare = room.getRoomType() == 1 && room.getMode() == 2;
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

        roomPlayerList.removeIf(rp -> rp.getPlayerId().equals(activePlayer.getId()));
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
                    gameSession.getClients().removeIf(c -> c.getPlayer() != null && c.getPlayer().getId().equals(activePlayer.getId()));
                }
                client.setActiveGameSession(null);
            }
        }

        if (playerPosition != -1) {
            if (notifyClients) {
                S2CLeaveRoomWithPositionPacket leaveRoomWithPositionPacket = new S2CLeaveRoomWithPositionPacket(playerPosition);
                getClientsInRoom(room.getRoomId()).forEach(c -> {
                    if (c.getPlayer() != null && c.getConnection() != null) {
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
                S2CRoomPositionChangeAnswerPacket roomPositionChangeAnswerPacket = new S2CRoomPositionChangeAnswerPacket((char) 0, newMaster.getPosition(), newMaster.getPosition());
                getClientsInRoom(room.getRoomId()).forEach(c -> {
                    if (c.getConnection() != null && !c.getActivePlayerId().equals(activePlayer.getId())) {
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
        roomPlayer.setPosition((short) 0);
        roomPlayer.setMaster(true);
        roomPlayer.setFitting(false);
        room.getRoomPlayerList().add(roomPlayer);

        addRoom(room);
        connection.getClient().setActiveRoom(room);
        connection.getClient().setInLobby(false);

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
