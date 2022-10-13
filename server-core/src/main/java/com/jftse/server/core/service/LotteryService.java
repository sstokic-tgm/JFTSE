package com.jftse.server.core.service;

import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.net.Connection;

import javax.annotation.PostConstruct;
import java.util.List;

public interface LotteryService {
    @PostConstruct
    void init();

    List<PlayerPocket> drawLottery(Connection connection, long playerPocketId, int productIndex);
}
