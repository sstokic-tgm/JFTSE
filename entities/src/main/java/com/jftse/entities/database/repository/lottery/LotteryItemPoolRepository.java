package com.jftse.entities.database.repository.lottery;

import com.jftse.entities.database.model.lottery.SLotteryItemPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LotteryItemPoolRepository extends JpaRepository<SLotteryItemPool, Long> {
    @Query(value = "SELECT lip FROM SLotteryItemPool lip JOIN FETCH lip.rarity sr JOIN FETCH lip.product WHERE lip.gachaIndex = :gachaIndex AND lip.status.id = :statusId")
    List<SLotteryItemPool> findByGachaIndexAndStatus(int gachaIndex, long statusId);
}
