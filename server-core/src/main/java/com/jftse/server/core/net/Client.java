package com.jftse.server.core.net;

import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Client<T extends Connection<?>> {
    protected T connection;

    protected Long accountId;
    protected Long activePlayerId;

    protected String ip;
    protected int port;

    public void setPlayer(Long id) {
        this.activePlayerId = id;
    }

    public void setAccount(Long id) {
        this.accountId = id;
    }

    public abstract Player getPlayer();

    public abstract void savePlayer(final Player player);

    public abstract Account getAccount();

    public abstract void saveAccount(final Account account);
}
