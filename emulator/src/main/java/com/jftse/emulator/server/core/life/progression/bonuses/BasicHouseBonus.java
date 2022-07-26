package com.jftse.emulator.server.core.life.progression.bonuses;

import com.jftse.emulator.server.core.life.progression.ExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonusDecorator;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.service.HomeService;
import com.jftse.entities.database.model.home.AccountHome;

public class BasicHouseBonus extends ExpGoldBonusDecorator {
    private final Long accountId;
    private final HomeService homeService;

    public BasicHouseBonus(ExpGoldBonus expGoldBonus, Long accountId) {
        super(expGoldBonus);
        this.accountId = accountId;

        this.homeService = ServiceManager.getInstance().getHomeService();
    }

    @Override
    public int calculateExp() {
        int calculatedExp = super.calculateExp();
        return calculatedExp + houseExp(calculatedExp);
    }

    @Override
    public int calculateGold() {
        int calculatedGold = super.calculateGold();
        return calculatedGold + houseGold(calculatedGold);
    }

    private int houseExp(int exp) {
        AccountHome accountHome = homeService.findAccountHomeByAccountId(this.accountId);
        if (accountHome == null)
            return 0;

        return (int) (exp * (accountHome.getBasicBonusExp() / 100.0));
    }

    private int houseGold(int gold) {
        AccountHome accountHome = homeService.findAccountHomeByAccountId(this.accountId);
        if (accountHome == null)
            return 0;

        return (int) (gold * (accountHome.getBasicBonusGold() / 100.0));
    }
}
