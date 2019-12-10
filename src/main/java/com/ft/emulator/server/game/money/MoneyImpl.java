package com.ft.emulator.server.game.money;

import com.ft.emulator.common.dao.GenericModelDao;
import com.ft.emulator.common.service.Service;
import com.ft.emulator.common.validation.ValidationException;
import com.ft.emulator.server.database.model.character.CharacterPlayer;

import javax.persistence.EntityManagerFactory;

public class MoneyImpl extends Service {

    private GenericModelDao<CharacterPlayer> characterPlayerDao;

    public MoneyImpl(EntityManagerFactory entityManagerFactory) {

        super(entityManagerFactory);

        characterPlayerDao = new GenericModelDao<>(entityManagerFactory, CharacterPlayer.class);
    }

    public CharacterPlayer updateMoney(CharacterPlayer characterPlayer, Integer gold) throws ValidationException {

        characterPlayer.setGold(characterPlayer.getGold() + gold);

        return characterPlayerDao.save(characterPlayer);
    }

    public CharacterPlayer setMoney(CharacterPlayer characterPlayer, Integer gold) throws ValidationException {

        characterPlayer.setGold(gold);

        return characterPlayerDao.save(characterPlayer);
    }

    public CharacterPlayer getCurrentMoneyForPlayer(CharacterPlayer characterPlayer) {
        return characterPlayerDao.find(characterPlayer.getId(), "account");
    }
}