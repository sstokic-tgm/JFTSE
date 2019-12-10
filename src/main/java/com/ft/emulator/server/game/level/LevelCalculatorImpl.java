package com.ft.emulator.server.game.level;

import com.ft.emulator.common.dao.GenericModelDao;
import com.ft.emulator.common.service.Service;
import com.ft.emulator.common.validation.ValidationException;
import com.ft.emulator.server.database.model.character.CharacterPlayer;
import com.ft.emulator.server.database.model.level.LevelExp;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

public class LevelCalculatorImpl extends Service {

    private GenericModelDao<CharacterPlayer> characterPlayerDao;

    public LevelCalculatorImpl(EntityManagerFactory entityManagerFactory) {

        super(entityManagerFactory);

	characterPlayerDao = new GenericModelDao<>(entityManagerFactory, CharacterPlayer.class);
    }

    public byte getLevel(int expValue, int currentExp, byte currentLevel) {

        if(currentLevel == 60) {
            return currentLevel;
	}

	EntityManager em = entityManagerFactory.createEntityManager();

	int newExp = expValue + currentExp;
	List<LevelExp> levelExpList = em.createQuery("FROM LevelExp lp WHERE level = :level ", LevelExp.class)
		.setParameter("level", currentLevel)
		.getResultList();

	if(levelExpList.isEmpty()) {
	    em.close();
	    return currentLevel;
	}
	em.close();

	LevelExp levelExp = levelExpList.get(0);

        return newExp >= levelExp.getExpValue() ? (byte)(currentLevel + 1) : currentLevel;
    }

    public CharacterPlayer setNewLevelStatusPoints(byte level, CharacterPlayer characterPlayer) throws ValidationException {

	if(level > characterPlayer.getLevel()) {
	    characterPlayer.setStatusPoints((byte)(characterPlayer.getStatusPoints() + 1));
	}
	characterPlayer.setLevel(level);

	return characterPlayerDao.save(characterPlayer);
    }
}