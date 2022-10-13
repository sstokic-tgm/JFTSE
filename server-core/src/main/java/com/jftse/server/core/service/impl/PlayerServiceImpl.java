package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.player.PlayerRepository;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.shared.packets.player.C2SPlayerCreatePacket;
import com.jftse.server.core.shared.packets.player.C2SPlayerStatusPointChangePacket;
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
@Transactional(isolation = Isolation.SERIALIZABLE)
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;


    @Override
    public Player save(Player player) {
        return playerRepository.save(player);
    }


    @Override
    public List<Player> findAll() {
        return playerRepository.findAll();
    }


    @Override
    public List<Player> findAllByAlreadyCreatedSorted(Sort sort) {
        return playerRepository.findAllByAlreadyCreatedTrue(sort);
    }


    @Override
    public List<Player> findAllByAlreadyCreatedPageable(Pageable pageable) {
        return playerRepository.findAllByAlreadyCreatedTrue(pageable).getContent();
    }


    @Override
    public List<Player> findAllByAccount(Account account) {
        return playerRepository.findAllByAccount_Id(account.getId());
    }


    @Override
    public int getPlayerRankingByName(String name, byte gameMode) {
        return playerRepository.getRankingByNameAndGameMode(name, gameMode);
    }


    @Override
    public Player findById(Long playerId) {
        Optional<Player> player = playerRepository.findById(playerId);
        return player.orElse(null);
    }


    @Override
    public Player findByIdFetched(Long playerId) {
        Optional<Player> player = playerRepository.findByIdFetched(playerId);
        return player.orElse(null);
    }


    @Override
    public Player findByName(String name) {
        List<Player> playerList = playerRepository.findAllByName(name);
        return playerList.size() != 0 ? playerList.get(0) : null;
    }


    @Override
    public Player findByNameFetched(String name) {
        Optional<Player> player = playerRepository.findAllByNameFetched(name);
        return player.orElse(null);
    }


    @Override
    public Player updateMoney(Player player, int gold) {
        player.setGold(player.getGold() + gold);
        return save(player);
    }


    @Override
    public Player setMoney(Player player, int gold) {
        player.setGold(gold);
        return save(player);
    }


    @Override
    public void remove(Long playerId) {
        playerRepository.deleteById(playerId);
    }

    @Override
    public boolean isStatusPointHack(C2SPlayerStatusPointChangePacket playerStatusPointChangePacket, Player player) {
        // checking them so we are not 'hacked'
        byte serverStatusPoints = player.getStatusPoints();
        byte clientStatusPoints = playerStatusPointChangePacket.getStatusPoints();

        byte strength = (byte) (playerStatusPointChangePacket.getStrength() - player.getStrength());
        byte stamina = (byte) (playerStatusPointChangePacket.getStamina() - player.getStamina());
        byte dexterity = (byte) (playerStatusPointChangePacket.getDexterity() - player.getDexterity());
        byte willpower = (byte) (playerStatusPointChangePacket.getWillpower() - player.getWillpower());

        byte newStatusPoints = (byte) (strength + stamina + dexterity + willpower + clientStatusPoints);

        return (serverStatusPoints - newStatusPoints) != 0;
    }

    @Override
    public boolean isStatusPointHack(C2SPlayerCreatePacket playerCreatePacket, Player player) {
        // checking them so we are not 'hacked'
        byte serverStatusPoints = player.getStatusPoints();
        byte clientStatusPoints = playerCreatePacket.getStatusPoints();

        byte strength = (byte) (playerCreatePacket.getStrength() - player.getStrength());
        byte stamina = (byte) (playerCreatePacket.getStamina() - player.getStamina());
        byte dexterity = (byte) (playerCreatePacket.getDexterity() - player.getDexterity());
        byte willpower = (byte) (playerCreatePacket.getWillpower() - player.getWillpower());

        byte newStatusPoints = (byte) (strength + stamina + dexterity + willpower + clientStatusPoints);

        return (serverStatusPoints - newStatusPoints) != 0;
    }
}
