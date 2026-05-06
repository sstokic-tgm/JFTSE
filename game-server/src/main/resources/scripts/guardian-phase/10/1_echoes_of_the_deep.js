var S2CMatchplayUseSkill = Java.type("com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayUseSkill");
var S2CChatRoomAnswerPacket = Java.type("com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket");
var PhaseUpdateResult = Java.type("com.jftse.emulator.server.core.matchplay.guardian.PhaseUpdateResult");

class EchoesOfTheDeep {
    constructor() {
        this.timeStarted = 0;
        this.finished = false;
        this.phaseStarted = false;
        this.isBossImmune = true;

        this.guardianSkillTimers = new Map();
        this.guardianAggroState = new Map();
        this.skillInterval = 10000; // 10 sec default
        this.aggroSkillInterval = 5000; // faster casting when aggressive
    }
}

let echoes = new EchoesOfTheDeep();

var phase = {
    getPhaseName: function () {
        return "Echoes of the Deep";
    },
    start: function () {
        const playerIds = game.getPlayerBattleStates().stream()
            .filter(p => p != null)
            .map(p => p.getId())
            .toList();

        for (let playerId of playerIds) {
            const playerConnection = gameManager.getConnectionByPlayerId(playerId);
            if (playerConnection) {
                let packet = new S2CChatRoomAnswerPacket(2, "Server", "The temple awakens... but not all voices are silent.");
                playerConnection.sendTCP(packet);
            }
        }

        echoes.timeStarted = Date.now();
        echoes.phaseStarted = true;

        const guardians = game.getGuardianBattleStates();
        let delayOffset = 0;

        for (let g of guardians) {
            g.getSkills().clear();
            if (!g.isBoss()) {
                echoes.guardianSkillTimers.set(g.getPosition(), Date.now() + delayOffset);
                delayOffset += 3750;

                echoes.guardianAggroState.set(g.getPosition(), false);
            }
        }
    },
    update: function (connection) {
        if (!echoes.phaseStarted || this.hasEnded()) return PhaseUpdateResult.CONTINUE;

        const guardians = game.getGuardianBattleStates();

        const boss = guardians.stream()
            .filter(g => g.isBoss())
            .findFirst()
            .orElse(null);

        if (!boss) return PhaseUpdateResult.ERROR;

        for (let g of guardians) {
            if (g.isBoss() || g.getCurrentHealth().get() < 1) continue;

            const position = g.getPosition();
            const lastCast = echoes.guardianSkillTimers.get(position) || 0;
            const now = Date.now();
            const isAggro = echoes.guardianAggroState.get(position);

            const interval = isAggro ? echoes.aggroSkillInterval : echoes.skillInterval;
            if (now - lastCast >= interval) {
                this.castGuardianSkill(g, isAggro, connection);
                echoes.guardianSkillTimers.set(position, now);
            }
        }

        const aliveGuards = guardians.stream()
            .filter(g => !g.isBoss() && g.getCurrentHealth().get() > 0)
            .toArray();

        if (aliveGuards.length === 1) {
            const soloPos = aliveGuards[0].getPosition();
            if (!echoes.guardianAggroState.get(soloPos)) {
                echoes.guardianAggroState.set(soloPos, true);
            }
        }

        const supportsDead = guardians.stream()
            .filter(g => !g.isBoss())
            .allMatch(g => g.getCurrentHealth().get() < 1);

        if (supportsDead) {
            let packet = new S2CChatRoomAnswerPacket(2, "Server", "The deep stirs. The true threat approaches...");
            gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);

            return PhaseUpdateResult.NEXT_PHASE;
        }

        return PhaseUpdateResult.CONTINUE;
    },
    castGuardianSkill: function (guardian, isAggro, connection) {
        const pos = guardian.getPosition();
        const skillService = serviceManager.getSkillService();
        const players = game.getPlayerBattleStates().stream()
            .filter(p => p != null && p.getPosition() < 4 && p.getCurrentHealth().get() > 0)
            .toArray();

        if (players.length === 0) return;

        const useSilence = Math.random() < 0.4;
        if (useSilence) {
            const silenceSkill = skillService.findSkillById(57);
            if (silenceSkill) {
                let target = players[Math.floor(Math.random() * players.length)];
                let packet = new S2CMatchplayUseSkill(
                    pos,
                    target.getPosition(),
                    silenceSkill.getId() - 1,
                    Math.floor(Math.random() * 127),
                    0, 0, 0
                );
                gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);
            }
        } else {
            const polymorphSkill = skillService.findSkillById(7);
            if (polymorphSkill) {
                let target = players[Math.floor(Math.random() * players.length)];
                let packet = new S2CMatchplayUseSkill(
                    pos,
                    target.getPosition(),
                    polymorphSkill.getId() - 1,
                    Math.floor(Math.random() * 127),
                    0, 0, 0
                );
                gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);
            }
        }

        if (isAggro) {
            const homingSkill = skillService.findSkillById(6);
            if (homingSkill) {
                let target = players[Math.floor(Math.random() * players.length)];
                let packet = new S2CMatchplayUseSkill(
                    pos,
                    target.getPosition(),
                    homingSkill.getId() - 1,
                    Math.floor(Math.random() * 127),
                    0, 0, 0
                );

                const event = eventHandler.createRunnableEvent(function () {
                    gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);
                }, 1250);
                eventHandler.offerJS(event);
            }
        }
    },
    end: function () {
        echoes.finished = true;
    },
    phaseTime: function () {
        return Date.now() - echoes.timeStarted;
    },
    playTime: function () {
        return 0;
    },
    hasEnded: function () {
        return echoes.finished || (this.playTime() !== 0 && this.phaseTime() > this.playTime());
    },
    getGuardianAttackLoopTime: function (guardian) {
        return -1; // we control GuardianAttackTask execution manually
    },
    onHeal: function (target, healAmount, isGuardian) {
        if (isGuardian) {
            return game.getGuardianCombatSystem().heal(target, healAmount);
        } else {
            return game.getPlayerCombatSystem().heal(target, healAmount);
        }
    },
    onDealDamage: function (attackingPlayer, targetGuardian, damage, hasAttackerDmgBuff, hasTargetDefBuff, skill) {
        const target = game.getGuardianBattleStateByPosition(targetGuardian);
        if (target?.isBoss() && echoes.isBossImmune) {
            return target.getCurrentHealth().get();
        }
        return game.getGuardianCombatSystem().dealDamage(attackingPlayer, targetGuardian, damage, hasAttackerDmgBuff, hasTargetDefBuff, skill);
    },
    onDealDamageToPlayer: function (attackingGuardian, targetPlayer, damageAmount, hasAttackerDmgBuff, hasTargetDefBuff, skill) {
        return game.getGuardianCombatSystem().dealDamageToPlayer(attackingGuardian, targetPlayer, damageAmount, hasAttackerDmgBuff, hasTargetDefBuff, skill);
    },
    onDealDamageOnBallLoss: function (attackerPos, targetPos, hasAttackerWillBuff) {
        const target = game.getGuardianBattleStateByPosition(targetPos);
        if (target?.isBoss() && echoes.isBossImmune) {
            return target.getCurrentHealth().get();
        }
        return game.getGuardianCombatSystem().dealDamageOnBallLoss(attackerPos, targetPos, hasAttackerWillBuff);
    },
    onDealDamageOnBallLossToPlayer: function (attackerPos, targetPos, hasAttackerWillBuff) {
        return game.getGuardianCombatSystem().dealDamageOnBallLossToPlayer(attackerPos, targetPos, hasAttackerWillBuff);
    }
}
