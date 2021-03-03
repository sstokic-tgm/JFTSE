package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.server.database.model.level.LevelExp;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.repository.level.LevelExpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class LevelService {
    private final LevelExpRepository levelExpRepository;

    private final PlayerService playerService;

    public byte getLevel(int expValue, int currentExp, byte currentLevel) {
        if (currentLevel == 60)
            return currentLevel;

        int newExp = expValue + currentExp;

        List<LevelExp> levelExpList = levelExpRepository.findAllByLevel(currentLevel);

        if (levelExpList.isEmpty())
            return currentLevel;

        LevelExp levelExp = levelExpList.get(0);

        return newExp >= levelExp.getExpValue() ? (byte) (currentLevel + 1) : currentLevel;
    }

    public Player setNewLevelStatusPoints(byte level, Player player) {
        if (level > player.getLevel())
            player.setStatusPoints((byte) (player.getStatusPoints() + 1));

        player.setLevel(level);

        return playerService.save(player);
    }
}
