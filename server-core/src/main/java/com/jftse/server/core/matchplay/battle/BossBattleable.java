package com.jftse.server.core.matchplay.battle;

import com.jftse.entities.database.model.battle.Skill;
import com.jftse.entities.database.model.battle.Skill2Guardians;

import java.util.List;

public interface BossBattleable<T> {
    T self();
    default void loadSkills() {
    }
    default Skill getRandomGuardianSkillBasedOnProbability() {
        return null;
    }
    default List<Skill2Guardians> getSkills() {
        return null;
    }

    default <ResultType> ResultType onUpdate(BossBattleableConsumer<T, ResultType> consumer) {
        return consumer.accept(self());
    }

    @FunctionalInterface
    interface BossBattleableConsumer<T, ResultType> {
        ResultType accept(T t);
    }
}
