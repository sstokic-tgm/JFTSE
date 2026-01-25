package com.jftse.emulator.server.net;

import com.jftse.entities.database.model.account.Account;
import com.jftse.server.core.net.Client;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;

public class FTClient extends Client<FTConnection> {
    @Getter
    @Setter
    private Long accountId;

    @Getter
    @Setter
    private boolean gameMaster = false;

    @Getter
    @Setter
    private Long lastPlayedPlayerId;

    @Getter
    @Setter
    private Integer accountStatus;

    private final AtomicBoolean isClientAlive = new AtomicBoolean(false);
    private final AtomicBoolean isLoginIn = new AtomicBoolean(false);

    public AtomicBoolean isClientAlive() {
        return isClientAlive;
    }

    public AtomicBoolean isLoginIn() {
        return isLoginIn;
    }

    public void prepareAccount(Account account) {
        this.accountId = account.getId();
        this.gameMaster = account.getGameMaster();
        this.lastPlayedPlayerId = account.getLastSelectedPlayerId();
        this.accountStatus = account.getStatus();
    }
}
