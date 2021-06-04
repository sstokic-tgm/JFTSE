package com.jftse.emulator.server.game.core.utils;

import com.jftse.emulator.server.database.model.battle.StaValue;
import com.jftse.emulator.server.database.model.battle.StrValue;
import com.jftse.emulator.server.database.model.battle.WillDamage;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.game.core.service.StaTableService;
import com.jftse.emulator.server.game.core.service.StrTableService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Scope("singleton")
@Getter
public class BattleUtilsService {
    private final StrTableService strTableService;
    private final StaTableService staTableService;

    /**
    * hp = base hp + 3hp for each level; level - 1 because at level 1 we have the base hp
    */
    public static int calculatePlayerHp(Player player) {
        return 200 + (3 * (player.getLevel() - 1));
    }

    public int calculateDmg(int str, int baseDmg, boolean hasStrBuff) {
        List<StrValue> strValues = this.strTableService.getStrValues();
        int additionalDmg = (int) strValues.stream().filter(x -> x.getStr() < str).count();
        int totalDmg = baseDmg - additionalDmg;
        if (hasStrBuff) {
            totalDmg -= Math.abs(totalDmg) * 0.2;
        }

        return totalDmg;
    }

    public int calculateDef(int sta, int dmg, boolean hasDefBuff) {
        List<StaValue> staValues = this.staTableService.getStaValues();
        int dmgToDeny = (int) staValues.stream().filter(x -> x.getSta() < sta).count();
        if (hasDefBuff) {
            dmgToDeny += dmg * 0.2;
        }

        return dmgToDeny;
    }

    public int calculateBallDamageByWill(WillDamage willDamage, boolean hasWillBuff) {
        int ballDamage = willDamage.getDamage();
        if (hasWillBuff) {
            ballDamage += ballDamage * 0.2;
        }

        return ballDamage;
    }
}
