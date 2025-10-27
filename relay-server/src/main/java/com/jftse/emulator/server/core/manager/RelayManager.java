package com.jftse.emulator.server.core.manager;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.Uptime;
import com.jftse.server.core.BuildInfoProperties;
import com.jftse.server.core.ServerLoopHandler;
import com.jftse.server.core.service.BlockedIPService;
import com.jftse.server.core.service.UptimeService;
import com.jftse.server.core.shared.packets.SMSGInitHandshake;
import com.jftse.server.core.util.GameTime;
import com.jftse.server.core.util.IntervalTimer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Getter
@Setter
@Log4j2
public class RelayManager implements ServerLoopHandler {
    private static RelayManager instance;

    @Autowired
    private BlockedIPService blockedIPService;
    @Autowired
    private UptimeService uptimeService;
    @Autowired
    private BuildInfoProperties revisionInfo;

    private ConcurrentLinkedQueue<FTConnection> addConnectionQueue;
    private ConcurrentLinkedDeque<FTClient> clients;
    private ConcurrentHashMap<Integer, ConcurrentLinkedDeque<FTClient>> sessionMap;
    private AtomicInteger playerCount = new AtomicInteger(0);
    private int maxPlayerCount = 0;

    private final Object lock = new Object();

    private IntervalTimer uptimeTimer = new IntervalTimer(TimeUnit.MINUTES.toMillis(10));

    @PostConstruct
    public void init() {
        instance = this;

        addConnectionQueue = new ConcurrentLinkedQueue<>();
        clients = new ConcurrentLinkedDeque<>();
        sessionMap = new ConcurrentHashMap<>();

        GameTime.updateGameTimers();

        Uptime uptime = new Uptime();
        uptime.setServerType(ServerType.RELAY_SERVER);
        uptime.setStartTime(GameTime.getStartTime().getEpochSecond());
        uptime.setUptime(0L);
        uptime.setRevision(revisionInfo.getFullVersion());
        uptimeService.save(uptime);

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static RelayManager getInstance() {
        return instance;
    }

    public void onExit() {
        uptimeService.updateUptimeAndMaxPlayers(GameTime.getUptimeSeconds(), getMaxPlayerCount(), ServerType.RELAY_SERVER, GameTime.getStartTime().getEpochSecond());

        log.info("RelayManager stopped");
    }

    public void addClient(FTClient client) {
        clients.add(client);
    }

    public void removeClient(FTClient client) {
        clients.remove(client);
        playerCount.getAndDecrement();
    }

    public void addClientToSession(final int sessionId, final FTClient client) {
        ConcurrentLinkedDeque<FTClient> clientList;
        if (sessionMap.containsKey(sessionId)) {
            clientList = sessionMap.get(sessionId);
        } else {
            clientList = new ConcurrentLinkedDeque<>();
        }
        clientList.add(client);
        sessionMap.put(sessionId, clientList);

        playerCount.getAndIncrement();
        maxPlayerCount = Math.max(maxPlayerCount, playerCount.get());
    }

    public void removeClient(final int sessionId, final FTClient client) {
        if (sessionMap.containsKey(sessionId)) {
            ConcurrentLinkedDeque<FTClient> clientList = sessionMap.get(sessionId);
            clientList.remove(client);
            removeClient(client);

            if (clientList.isEmpty())
                sessionMap.remove(sessionId);
        }
    }

    public final List<FTClient> getClientsInSession(final int sessionId) {
        return new ArrayList<>(sessionMap.get(sessionId));
    }

    public void queueConnection(FTConnection connection) {
        addConnectionQueue.offer(connection);
    }

    @Override
    public void update(long diff) {
        GameTime.updateGameTimers();

        if (uptimeTimer.getCurrent() >= 0)
            uptimeTimer.update(diff);
        else
            uptimeTimer.setCurrent(0);

        updateSessions(diff);

        if (uptimeTimer.passed()) {
            long uptimeSeconds = GameTime.getUptimeSeconds();
            int maxOnlinePlayers = getMaxPlayerCount();
            uptimeTimer.reset();

            uptimeService.updateUptimeAndMaxPlayers(uptimeSeconds, maxOnlinePlayers, ServerType.RELAY_SERVER, GameTime.getStartTime().getEpochSecond());
        }
    }

    private void updateSessions(long diff) {
        while (!addConnectionQueue.isEmpty()) {
            FTConnection conn = addConnectionQueue.poll();
            if (conn != null) {
                initializeConnection(conn);
            }
        }

        final ConcurrentLinkedDeque<FTClient> clientsSnapshot = new ConcurrentLinkedDeque<>(getClients());
        for (FTClient client : clientsSnapshot) {
            FTConnection conn = client.getConnection();
            if (conn != null && !conn.update(diff)) {
                conn.close();
            }
        }
    }

    private void initializeConnection(FTConnection conn) {
        FTClient client = new FTClient();
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
    }
}
