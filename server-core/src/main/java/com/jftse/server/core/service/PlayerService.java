package com.jftse.server.core.service;

import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.shared.packets.auth.CMSGPlayerCreate;
import com.jftse.server.core.shared.packets.player.C2SPlayerStatusPointChangePacket;
import com.jftse.server.core.shared.packets.player.CMSGChangePlayerStatPoints;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface PlayerService {
    Player save(Player player);

    List<Player> findAll();

    List<Player> findAllByAlreadyCreatedSorted(Sort sort);

    List<Player> findAllByAlreadyCreatedPageable(Pageable pageable);

    List<Player> findAllByAccount(Account account);
    List<Player> findAllByAccount(Long accountId);

    int getPlayerRankingByName(String name, byte gameMode);

    int getTutorialProgressSucceededCountByAccount(Long accountId);

    Player findById(Long playerId);

    Player getPlayerRef(Long playerId);

    Player findByIdFetched(Long playerId);

    Player findWithEquipmentById(Long playerId);
    Player findWithAccountById(Long playerId);
    Player findWithPocketById(Long playerId);
    Player findWithStatisticById(Long playerId);

    List<Player> getPlayerListByAccountId(Long accountId);

    Player findByName(String name);

    Player findByNameFetched(String name);

    Player updateMoney(Player player, int gold);

    Player setMoney(Player player, int gold);

    void remove(Long playerId);

    boolean isStatusPointHack(CMSGPlayerCreate playerCreatePacket, Player player);

    boolean isStatusPointHack(CMSGChangePlayerStatPoints playerStatPointsPacket, Player player);
}
