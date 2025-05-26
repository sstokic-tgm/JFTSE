package com.jftse.emulator.server.core.manager;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.account.Account;
import com.jftse.proto.auth.UpdateAccountRequest;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.thread.ThreadManager;
import com.jftse.server.core.util.AccountAction;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Getter
@Setter
@Log4j2
public class AuthenticationManager {
    private static AuthenticationManager instance;

    private ConcurrentLinkedDeque<FTClient> clients;
    private AtomicBoolean running;
    private Future<?> authenticationTask;

    private BlockingQueue<UpdateAccountRequest> updateAccountQueue;

    @Autowired
    private ServiceManager serviceManager;
    @Autowired
    private ThreadManager threadManager;

    private String motd;

    @PostConstruct
    public void init() {
        instance = this;

        clients = new ConcurrentLinkedDeque<>();
        updateAccountQueue = new LinkedBlockingQueue<>();

        running = new AtomicBoolean(true);

        setupGlobalTasks();

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public void onExit() {
        if (running.compareAndSet(true, false)) {
            if (authenticationTask != null && authenticationTask.cancel(false)) {
                log.info("AuthenticationTask stopped");
            }

            log.info("Closing all connections");
            for (FTClient client : clients) {
                client.getConnection().close();
            }

            log.info("Clearing queue for update account requests");
            while (!updateAccountQueue.isEmpty()) {
                try {
                    UpdateAccountRequest request = updateAccountQueue.poll();
                    if (request != null) {
                        processUpdateAccountRequest(request);
                    }
                } catch (Exception ex) {
                    log.error("Error processing remaining request: {}", ex.getMessage(), ex);
                }
            }

            log.info("All connections closed");

            clients.clear();

            log.info("AuthenticationManager stopped");
        }
    }

    public static AuthenticationManager getInstance() {
        return instance;
    }

    public void addClient(FTClient client) {
        clients.add(client);
    }

    public void removeClient(FTClient client) {
        clients.remove(client);
    }

    private void setupGlobalTasks() {
        authenticationTask = threadManager.submit(() -> {
            while (running.get()) {
                try {
                    processUpdateAccountRequestQueue();
                } catch (Exception ex) {
                    log.error(String.format("Exception in runnable thread: %s", ex.getMessage()), ex);
                }
            }
            log.info("AuthenticationTask stopped");
        });
        log.info("AuthenticationTask started");
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
            case null, default -> log.warn("Unknown account action {} for account ID: {}", action, request.getAccountId());
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
            UpdateAccountRequest request = updateAccountQueue.take();
            if (request != null) {
                final UpdateAccountRequest validRequest = getValidUpdateAccountRequest(request);
                processUpdateAccountRequest(validRequest);
            }
            // save cpu cycles
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error processing update account request queue: {}", e.getMessage(), e);

            while (!updateAccountQueue.isEmpty()) {
                try {
                    UpdateAccountRequest request = updateAccountQueue.poll();
                    if (request != null) {
                        processUpdateAccountRequest(request);
                    }
                } catch (Exception ex) {
                    log.error("Error processing remaining request: {}", ex.getMessage(), ex);
                }
            }
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
}
