package com.jftse.emulator.server.core.manager;

import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.EconomySnapshot;
import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.Uptime;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.proto.auth.UpdateAccountRequest;
import com.jftse.server.core.BuildInfoProperties;
import com.jftse.server.core.jdbc.JdbcUtil;
import com.jftse.server.core.ServerLoopHandler;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.ServerConfService;
import com.jftse.server.core.shared.packets.SMSGInitHandshake;
import com.jftse.server.core.shared.packets.SMSGServerNotice;
import com.jftse.server.core.thread.ThreadManager;
import com.jftse.server.core.util.AccountAction;
import com.jftse.server.core.util.GameTime;
import com.jftse.server.core.util.IntervalTimer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Getter
@Setter
@Log4j2
public class AuthenticationManager implements ServerLoopHandler {
    @Getter
    private static AuthenticationManager instance;

    private ConcurrentLinkedQueue<FTConnection> addConnectionQueue;
    private ConcurrentLinkedDeque<FTClient> clients;
    private AtomicInteger playerCount = new AtomicInteger(0);
    private int maxPlayerCount = 0;

    private BlockingQueue<UpdateAccountRequest> updateAccountQueue;

    @Autowired
    private ServiceManager serviceManager;
    @Autowired
    private ThreadManager threadManager;
    @Autowired
    private JdbcUtil jdbcUtil;

    @Autowired
    private BuildInfoProperties revisionInfo;
    @Autowired
    private ServerConfService serverConfService;

    private String motd;

    private IntervalTimer[] timers = new IntervalTimer[ServerTimers.COUNT];

    @PostConstruct
    public void init() {
        instance = this;

        clients = new ConcurrentLinkedDeque<>();
        updateAccountQueue = new LinkedBlockingQueue<>();
        addConnectionQueue = new ConcurrentLinkedQueue<>();

        GameTime.updateGameTimers();
        initTimers();

        Uptime uptime = new Uptime();
        uptime.setServerType(ServerType.AUTH_SERVER);
        uptime.setStartTime(GameTime.getStartTime().getEpochSecond());
        uptime.setUptime(0L);
        uptime.setRevision(revisionInfo.getFullVersion());
        serviceManager.getUptimeService().save(uptime);

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public void onExit() {
        log.info("Closing all connections");

        for (FTClient client : clients) {
            client.getConnection().close();
        }

        log.info("Clearing leftover accounts still marked as logged in");
        while (!clients.isEmpty()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
        resetLogins();
        log.info("Leftover accounts cleared");

        serviceManager.getUptimeService().updateUptimeAndMaxPlayers(GameTime.getUptimeSeconds(), getMaxPlayerCount(), ServerType.AUTH_SERVER, GameTime.getStartTime().getEpochSecond());

        log.info("AuthenticationManager stopped");
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

        if (timers[ServerTimers.SUPDATE_ECONOMY.value()].passed()) {
            timers[ServerTimers.SUPDATE_ECONOMY.value()].reset();

            jdbcUtil.execute(em -> {
                Long totalGold = Optional.ofNullable(
                        em.createQuery("SELECT SUM(p.gold) FROM Player p WHERE p.alreadyCreated = true", Long.class)
                                .getSingleResult()
                ).orElse(0L);

                Long totalAp = Optional.ofNullable(
                        em.createQuery("SELECT SUM(a.ap) FROM Account a", Long.class).getSingleResult()
                ).orElse(0L);

                Long activePlayers = Optional.ofNullable(
                        em.createQuery("SELECT COUNT(p) FROM Player p WHERE p.alreadyCreated = true", Long.class).getSingleResult()
                ).orElse(0L);

                Long activeAccounts = Optional.ofNullable(
                        em.createQuery("SELECT COUNT(DISTINCT p.account.id) FROM Player p WHERE p.alreadyCreated = true", Long.class).getSingleResult()
                ).orElse(0L);

                EconomySnapshot snap = new EconomySnapshot();
                snap.setDateTime(GameTime.getDateAndTime());
                snap.setTotalGold(totalGold);
                snap.setTotalAp(totalAp);
                snap.setActivePlayers(activePlayers.intValue());
                snap.setAvgGoldPerPlayer(activePlayers > 0 ? totalGold / activePlayers : 0);
                snap.setAvgApPerAccount(activeAccounts > 0 ? totalAp / activeAccounts : 0);

                em.persist(snap);
            });
        }

        updateSessions(diff);
        processUpdateAccountRequestQueue();

        if (timers[ServerTimers.SUPDATE_UPTIME.value()].passed()) {
            long uptimeSeconds = GameTime.getUptimeSeconds();
            int maxOnlinePlayers = getMaxPlayerCount();
            timers[ServerTimers.SUPDATE_UPTIME.value()].reset();

            serviceManager.getUptimeService().updateUptimeAndMaxPlayers(uptimeSeconds, maxOnlinePlayers, ServerType.AUTH_SERVER, GameTime.getStartTime().getEpochSecond());
        }
    }

    private void resetLogins() {
        List<Account> loggedInAccounts = serviceManager.getAuthenticationService().findByStatus((int) AuthenticationServiceImpl.ACCOUNT_ALREADY_LOGGED_IN);
        for (Account account : loggedInAccounts) {
            account.setStatus((int) AuthenticationServiceImpl.SUCCESS);
            account.setLoggedInServer(ServerType.NONE);
            try {
                serviceManager.getAuthenticationService().updateAccount(account);
                log.info("Account ID {} cleared from logged in status", account.getId());
            } catch (Exception e) {
                log.error("Failed to clear account ID {}: {}", account.getId(), e.getMessage(), e);
            }
        }

        List<Player> playerList = new ArrayList<>();
        jdbcUtil.execute(em -> {
            List<Player> tmpList = em.createQuery("SELECT p FROM Player p WHERE p.online = :online", Player.class)
                    .setParameter("online", true)
                    .getResultList();
            playerList.addAll(tmpList);
        });

        for (Player player : playerList) {
            player.setOnline(false);
            try {
                serviceManager.getPlayerService().save(player);
                log.info("Player ID {} cleared from online status", player.getId());
            } catch (Exception e) {
                log.error("Failed to clear player ID {}: {}", player.getId(), e.getMessage(), e);
            }
        }
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

    public void addUpdateAccountRequest(UpdateAccountRequest request) {
        updateAccountQueue.offer(request);
    }

    public boolean removeUpdateAccountRequest(UpdateAccountRequest request) {
        return updateAccountQueue.remove(request);
    }

    private void processUpdateAccountRequest(UpdateAccountRequest request) {
        Account account = serviceManager.getAuthenticationService().findAccountById(request.getAccountId());
        if (account == null) {
            log.warn("Account with ID {} not found for update request", request.getAccountId());
            return;
        }

        final AccountAction action = AccountAction.fromValue(request.getAccountAction().getAction());
        switch (action) {
            case LOGIN -> {
                log.info("Processing login request for account ID: {}", request.getAccountId());
                account.setStatus((int) AuthenticationServiceImpl.ACCOUNT_ALREADY_LOGGED_IN);
                account.setLastLogin(new Date(request.getTimestamp()));
                account.setLoggedInServer(ServerType.fromValue(request.getServer()));
            }
            case LOGOUT, DISCONNECT -> {
                log.info("Processing logout request for account ID: {}", request.getAccountId());
                account.setStatus((int) AuthenticationServiceImpl.SUCCESS);
                account.setLoggedInServer(ServerType.NONE);
                account.setLogoutServer(ServerType.fromValue(request.getServer()));
            }
            case RELOG -> {
                log.info("Processing relog request for account ID: {}", request.getAccountId());
                account.setStatus((int) AuthenticationServiceImpl.ACCOUNT_ALREADY_LOGGED_IN);
                account.setLoggedInServer(ServerType.fromValue(request.getServer()));
            }
            case null, default ->
                    log.warn("Unknown account action {} for account ID: {}", action, request.getAccountId());
        }

        try {
            serviceManager.getAuthenticationService().updateAccount(account);
            log.info("Account ID {} updated successfully with action {}", request.getAccountId(), action);
        } catch (Exception e) {
            log.error("Failed to update account ID {}: {}", request.getAccountId(), e.getMessage(), e);
            addUpdateAccountRequest(request);
        }
    }

    private void processUpdateAccountRequestQueue() {
        try {
            for (UpdateAccountRequest request = updateAccountQueue.poll(); request != null; request = updateAccountQueue.poll()) {
                final UpdateAccountRequest validRequest = getValidUpdateAccountRequest(request);
                processUpdateAccountRequest(validRequest);
            }
        } catch (Exception ex) {
            log.error("Error processing update account request: {}", ex.getMessage(), ex);
        }
    }

    private UpdateAccountRequest getValidUpdateAccountRequest(UpdateAccountRequest request) {
        final List<UpdateAccountRequest> batch = new ArrayList<>();
        updateAccountQueue.drainTo(batch);
        batch.add(request);

        batch.sort(Comparator.comparingLong(UpdateAccountRequest::getTimestamp));

        final UpdateAccountRequest toProcess = batch.removeFirst();
        for (UpdateAccountRequest tmp : batch) {
            addUpdateAccountRequest(tmp);
        }

        return toProcess;
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
                removeClient(client);
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

        final String motd = AuthenticationManager.getInstance().getMotd();
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
        timers[ServerTimers.SUPDATE_ECONOMY.value()].setInterval(TimeUnit.HOURS.toMillis(serverConfService.get("UpdateEconomyInterval", Integer.class)));
    }


    public enum ServerTimers {
        SUPDATE_ECONOMY,
        SUPDATE_UPTIME;

        public static final int COUNT = values().length;

        public int value() {
            return this.ordinal();
        }
    }
}
