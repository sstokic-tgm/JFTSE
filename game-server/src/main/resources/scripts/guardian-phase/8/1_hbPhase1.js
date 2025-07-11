var PhaseUpdateResult = Java.type("com.jftse.emulator.server.core.matchplay.guardian.PhaseUpdateResult");

class Phase1 {
    constructor() {
        this.timeStarted = 0;
        this.finished = false;
        this.phaseStarted = false;
        this.isBossImmune = true;
    }
}

let phase1 = new Phase1();

var phase = {
    getPhaseName: function () {
        return "Phase 1";
    },
    start: function () {
        phase1.timeStarted = Date.now();
        phase1.phaseStarted = true;
    },
    update: function (connection) {
        if (!phase1.phaseStarted || this.hasEnded()) return PhaseUpdateResult.CONTINUE;

        let guardianBattleStates = game.getGuardianBattleStates();
        let nonBossGuardiansAllDead = false;

        for (let guardianBattleState of guardianBattleStates) {
            if (phase1.isBossImmune) {
                if (guardianBattleState.isBoss()) {
                    continue;
                }

                if (guardianBattleState.getCurrentHealth().get() < 1) {
                    nonBossGuardiansAllDead = true;
                } else {
                    nonBossGuardiansAllDead = false;
                    break;
                }
            }
        }

        if (nonBossGuardiansAllDead) {
            return PhaseUpdateResult.NEXT_PHASE;
        }

        return PhaseUpdateResult.CONTINUE;
    },
    end: function () {
        phase1.finished = true;
    },
    phaseTime: function () {
        return Date.now() - phase1.timeStarted;
    },
    playTime: function () {
        return 0;
    },
    hasEnded: function () {
        return (phase1.finished) || (this.playTime() !== 0 && this.phaseTime() > this.playTime());
    },
    getGuardianAttackLoopTime: function (guardian) {
        let attackLoopTime = -1;
        /*if (guardian.isBoss() && phase1.isBossImmune) {
            return attackLoopTime;
        }*/
        if (guardian.isBoss()) {
            attackLoopTime = 4 * 1000;
        } else {
            // 6 - 12 seconds
            attackLoopTime = Math.floor(Math.random() * 7 + 6) * 1000;
        }
        return attackLoopTime;
    },
    onHeal: function (target, healAmount, isGuardian) {
        if (isGuardian) {
            return game.getGuardianCombatSystem().heal(target, healAmount);
        } else {
            return game.getPlayerCombatSystem().heal(target, healAmount);
        }
    },
    onDealDamage: function (attackingPlayer, targetGuardian, damage, hasAttackerDmgBuff, hasTargetDefBuff, skill) {
        let targetGuardianState = game.getGuardianBattleStateByPosition(targetGuardian);
        if (targetGuardianState) {
            if (phase1.isBossImmune && targetGuardianState.isBoss()) {
                return targetGuardianState.getCurrentHealth().get();
            }
        }
        return game.getGuardianCombatSystem().dealDamage(attackingPlayer, targetGuardian, damage, hasAttackerDmgBuff, hasTargetDefBuff, skill);
    },
    onDealDamageToPlayer: function (attackingGuardian, targetPlayer, damageAmount, hasAttackerDmgBuff, hasTargetDefBuff, skill) {
        return game.getGuardianCombatSystem().dealDamageToPlayer(attackingGuardian, targetPlayer, damageAmount, hasAttackerDmgBuff, hasTargetDefBuff, skill);
    },
    onDealDamageOnBallLoss: function (attackerPos, targetPos, hasAttackerWillBuff) {
        let targetGuardianState = game.getGuardianBattleStateByPosition(targetPos);
        if (targetGuardianState) {
            if (phase1.isBossImmune && targetGuardianState.isBoss()) {
                return targetGuardianState.getCurrentHealth().get();
            }
        }
        return game.getGuardianCombatSystem().dealDamageOnBallLoss(attackerPos, targetPos, hasAttackerWillBuff);
    },
    onDealDamageOnBallLossToPlayer: function (attackerPos, targetPos, hasAttackerWillBuff) {
        return game.getGuardianCombatSystem().dealDamageOnBallLossToPlayer(attackerPos, targetPos, hasAttackerWillBuff);
    }
}