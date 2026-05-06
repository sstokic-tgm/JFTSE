package com.jftse.emulator.server.core.service;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.life.lottery.GachaOpenResult;
import com.jftse.emulator.server.net.FTClient;

import java.util.List;
import java.util.function.BiConsumer;

public interface LotteryServiceV2 {
    List<GachaOpenResult> openGacha(FTClient client, String gachaName, int count, BiConsumer<Integer, GachaOpenResult> consumer) throws ValidationException;
    GachaOpenResult openGacha(FTClient client, long playerPocketId, int productIndex) throws ValidationException;
}
