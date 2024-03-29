package com.jftse.server.core.service.impl;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.entities.database.model.level.LevelExp;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.level.LevelExpRepository;
import com.jftse.server.core.service.LevelService;
import com.jftse.server.core.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class LevelServiceImpl implements LevelService {
    private final LevelExpRepository levelExpRepository;

    private final PlayerService playerService;

    private final ConfigService configService;

    @Override
    public byte getLevel(int expValue, int currentExp, byte currentLevel) {
        if (currentLevel == configService.getValue("player.level.max", 60))
            return currentLevel;

        int newExp = expValue + currentExp;

        List<LevelExp> levelExpList = levelExpRepository.findAllByLevel(currentLevel);

        if (levelExpList.isEmpty())
            return currentLevel;

        LevelExp levelExp = levelExpList.get(0);

        if (newExp >= levelExp.getExpValue()) {
            List<LevelExp> newLevelExp = findAllByExpValue(newExp);
            return (byte) (newLevelExp.get(0).getLevel() + 1);
        } else {
            return currentLevel;
        }
    }

    @Override
    public List<LevelExp> findAllByExpValue(Integer expValue) {
        return levelExpRepository.findAllByExpValueIsLessThanEqualOrderByExpValueDesc(expValue);
    }

    @Override
    public Player setNewLevelStatusPoints(byte level, Player player) {
        if (level > player.getLevel() && level <= 65)
            player.setStatusPoints((byte) (player.getStatusPoints() + (level - player.getLevel())));

        player.setLevel(level);

        return playerService.save(player);
    }
}
