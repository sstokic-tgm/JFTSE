package com.jftse.entities.database.repository.lottery;

import com.jftse.entities.database.model.lottery.PlayerLotteryProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerLotteryProgressRepository extends JpaRepository<PlayerLotteryProgress, Long> {
    Optional<PlayerLotteryProgress> findByPlayer_IdAndGachaIndex(long player_id, int gacha_index);
}
