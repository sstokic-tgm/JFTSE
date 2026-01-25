package com.jftse.server.core.service;

import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.net.Client;
import com.jftse.server.core.net.Connection;

import javax.annotation.PostConstruct;
import java.util.List;

public interface LotteryService<C extends Connection<? extends Client<C>>> {
    @PostConstruct
    void init();

    List<PlayerPocket> drawLottery(C connection, long playerPocketId, int productIndex);
}
