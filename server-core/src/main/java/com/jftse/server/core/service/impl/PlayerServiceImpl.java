package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.player.PlayerRepository;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.shared.packets.auth.CMSGPlayerCreate;
import com.jftse.server.core.shared.packets.player.CMSGChangePlayerStatPoints;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Player save(Player player) {
        return playerRepository.save(player);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Player> findAll() {
        return playerRepository.findAll();
    }


    @Override
    @Transactional(readOnly = true)
    public List<Player> findAllByAlreadyCreatedSorted(Sort sort) {
        return playerRepository.findAllByAlreadyCreatedTrue(sort);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Player> findAllByAlreadyCreatedPageable(Pageable pageable) {
        return playerRepository.findAllByAlreadyCreatedTrue(pageable).getContent();
    }


    @Override
    @Transactional(readOnly = true)
    public List<Player> findAllByAccount(Account account) {
        return playerRepository.findAllByAccount_Id(account.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Player> findAllByAccount(Long accountId) {
        return playerRepository.findAllByAccount_Id(accountId);
    }


    @Override
    @Transactional
    public int getPlayerRankingByName(String name, byte gameMode) {
        return playerRepository.getRankingByNameAndGameMode(name, gameMode);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTutorialProgressSucceededCountByAccount(Long accountId) {
        return playerRepository.getTutorialProgressSucceededCount(accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public Player findById(Long playerId) {
        Optional<Player> player = playerRepository.findById(playerId);
        return player.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Player getPlayerRef(Long playerId) {
        return playerRepository.getReferenceById(playerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Player findByIdFetched(Long playerId) {
        Optional<Player> player = playerRepository.findByIdFetched(playerId);
        return player.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Player findWithEquipmentById(Long playerId) {
        Optional<Player> player = playerRepository.findWithEquipmentById(playerId);
        return player.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Player findWithAccountById(Long playerId) {
        Optional<Player> player = playerRepository.findWithAccountById(playerId);
        return player.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Player findWithPocketById(Long playerId) {
        Optional<Player> player = playerRepository.findWithPocketById(playerId);
        return player.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Player findWithStatisticById(Long playerId) {
        Optional<Player> player = playerRepository.findWithStatisticById(playerId);
        return player.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Player> getPlayerListByAccountId(Long accountId) {
        return playerRepository.getPlayerListByAccountId(accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public Player findByName(String name) {
        List<Player> playerList = playerRepository.findAllByName(name);
        for (Player player : playerList) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }


    @Override
    @Transactional(readOnly = true)
    public Player findByNameFetched(String name) {
        List<Player> playerList = playerRepository.findAllByNameFetched(name);
        for (Player player : playerList) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Player updateMoney(Player player, int gold) {
        player.setGold(player.getGold() + gold);
        return save(player);
    }


    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Player setMoney(Player player, int gold) {
        player.setGold(gold);
        return save(player);
    }


    @Override
    @Transactional
    public void remove(Long playerId) {
        playerRepository.deleteById(playerId);
    }

    @Override
    public boolean isStatusPointHack(CMSGPlayerCreate playerCreatePacket, Player player) {
        byte serverStatusPoints = player.getStatusPoints();
        byte clientStatusPoints = playerCreatePacket.getStatusPoints();

        byte strength = (byte) (playerCreatePacket.getStrength() - player.getStrength());
        byte stamina = (byte) (playerCreatePacket.getStamina() - player.getStamina());
        byte dexterity = (byte) (playerCreatePacket.getDexterity() - player.getDexterity());
        byte willpower = (byte) (playerCreatePacket.getWillpower() - player.getWillpower());

        byte newStatusPoints = (byte) (strength + stamina + dexterity + willpower + clientStatusPoints);

        return (serverStatusPoints - newStatusPoints) != 0;
    }

    @Override
    public boolean isStatusPointHack(CMSGChangePlayerStatPoints playerStatPointsPacket, Player player) {
        byte serverStatusPoints = player.getStatusPoints();
        byte clientStatusPoints = playerStatPointsPacket.getStatusPoints();

        byte strength = (byte) (playerStatPointsPacket.getStrength() - player.getStrength());
        byte stamina = (byte) (playerStatPointsPacket.getStamina() - player.getStamina());
        byte dexterity = (byte) (playerStatPointsPacket.getDexterity() - player.getDexterity());
        byte willpower = (byte) (playerStatPointsPacket.getWillpower() - player.getWillpower());

        byte newStatusPoints = (byte) (strength + stamina + dexterity + willpower + clientStatusPoints);

        return (serverStatusPoints - newStatusPoints) != 0;
    }
}
