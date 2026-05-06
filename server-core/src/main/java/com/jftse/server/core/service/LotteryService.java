package com.jftse.server.core.service;

import com.jftse.entities.database.model.lottery.LotteryItemDto;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;

import java.util.List;

public interface LotteryService {
    PlayerPocket drawLottery(List<LotteryItemDto> lotteryItemList, Pocket pocket);

    List<LotteryItemDto> getLotteryItemsByGachaIndex(int playerType, int gachaIndex);
}
