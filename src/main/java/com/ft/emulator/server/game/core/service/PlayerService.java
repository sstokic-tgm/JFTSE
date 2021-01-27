package com.ft.emulator.server.game.core.service;

import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.database.repository.player.PlayerRepository;
import com.ft.emulator.server.game.core.packet.packets.player.C2SPlayerStatusPointChangePacket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class PlayerService {

    private final PlayerRepository playerRepository;

    public Player save(Player player) {
        return playerRepository.save(player);
    }

    public List<Player> findAll() {
        return playerRepository.findAll();
    }

    public Player findById(Long playerId) {
        Optional<Player> player = playerRepository.findById(playerId);
        return player.orElse(null);
    }

    public Player findByIdFetched(Long playerId) {
        Optional<Player> player = playerRepository.findByIdFetched(playerId);
        return player.orElse(null);
    }

    public Player findByName(String name) {
        List<Player> playerList = playerRepository.findAllByName(name);
        return playerList.size() != 0 ? playerList.get(0) : null;
    }

    public Player updateMoney(Player player, int gold) {
        player.setGold(player.getGold() + gold);
        return save(player);
    }

    public Player setMoney(Player player, int gold) {
        player.setGold(gold);
        return save(player);
    }

    public void remove(Long playerId) {
        playerRepository.deleteById(playerId);
    }

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
}
