package com.jftse.server.core.service;

import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.shared.packets.player.C2SPlayerCreatePacket;
import com.jftse.server.core.shared.packets.player.C2SPlayerStatusPointChangePacket;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface PlayerService {
    Player save(Player player);

    List<Player> findAll();

    List<Player> findAllByAlreadyCreatedSorted(Sort sort);

    List<Player> findAllByAlreadyCreatedPageable(Pageable pageable);

    List<Player> findAllByAccount(Account account);

    int getPlayerRankingByName(String name, byte gameMode);

    Player findById(Long playerId);

    Player findByIdFetched(Long playerId);

    Player findByName(String name);

    Player findByNameFetched(String name);

    Player updateMoney(Player player, int gold);

    Player setMoney(Player player, int gold);

    void remove(Long playerId);

    boolean isStatusPointHack(C2SPlayerStatusPointChangePacket playerStatusPointChangePacket, Player player);

    boolean isStatusPointHack(C2SPlayerCreatePacket playerCreatePacket, Player player);
}
