package com.jftse.emulator.server.core.manager;

import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.event.PacketEventHandler;
import com.jftse.emulator.server.core.matchplay.event.RunnableEventHandler;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.database.model.player.Player;
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
import java.util.concurrent.ConcurrentLinkedDeque;
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
    private PacketEventHandler packetEventHandler;
    @Autowired
    private RunnableEventHandler runnableEventHandler;

    @Autowired
    private ThreadManager threadManager;

    private AtomicBoolean running;

    private ConcurrentLinkedDeque<Client> clients;
    private ConcurrentLinkedDeque<Room> rooms;

    @PostConstruct
    public void init() {
        instance = this;

        running = new AtomicBoolean(true);
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
                .map(Client::getActivePlayer)
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

    private void setupGlobalTasks() {
        threadManager.newTask(() -> {
            log.info("Queued packet handling started");
            while (running.get()) {
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
            while (running.get()) {
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
        running.compareAndSet(true, false);
    }
}
