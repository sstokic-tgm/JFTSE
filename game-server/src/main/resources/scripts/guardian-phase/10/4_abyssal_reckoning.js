var S2CMatchplayUseSkill = Java.type("com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayUseSkill");
var S2CMatchplayDealDamage = Java.type("com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayDealDamage");
var S2CChatRoomAnswerPacket = Java.type("com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket");
var PhaseUpdateResult = Java.type("com.jftse.emulator.server.core.matchplay.guardian.PhaseUpdateResult");
var ApplyDoTTask = Java.type("com.jftse.emulator.server.core.task.ApplyDoTTask");

class AbyssalReckoning {
    constructor() {
        this.timeStarted = 0;
        this.finished = false;
        this.phaseStarted = false;
        this.bossSkillTimers = new Map();
        this.guardianSkillTimers = new Map();
        this.guardianAggroState = new Map();
        this.skillInterval = 10000;
        this.aggroSkillInterval = 5000;
        this.fullHealUsed = false;
        this.dialogSent = false;
        this.resurrectedGuardians = new Set();
        this.scheduledRebirthGuardians = new Set();
        this.rebirthUsed = false;
    }
}

let abyss = new AbyssalReckoning();

var phase = {
    getPhaseName: function () {
        return "Abyssal Reckoning";
    },
    start: function () {
        abyss.timeStarted = Date.now();
        abyss.phaseStarted = true;

        const guardians = game.getGuardianBattleStates();
        for (let g of guardians) {
            g.getSkills().clear();
            if (!g.isBoss()) {
                abyss.guardianSkillTimers.set(g.getPosition(), Date.now());
                abyss.guardianAggroState.set(g.getPosition(), false);
            }
        }

        abyss.bossSkillTimers.set("Dot", Date.now());
        abyss.bossSkillTimers.set("Earth", Date.now());
        abyss.bossSkillTimers.set("Pillar", Date.now());
        abyss.bossSkillTimers.set("Storm", Date.now());
        abyss.bossSkillTimers.set("Laser", Date.now());
        abyss.bossSkillTimers.set("HomingBall", Date.now());
    },
    update: function (connection) {
        if (!abyss.phaseStarted || this.hasEnded()) return PhaseUpdateResult.CONTINUE;
        try {
            const now = Date.now();
            const boss = game.getGuardianBattleStates().stream().filter(g => g.isBoss()).findFirst().orElse(null);
            if (!boss) return PhaseUpdateResult.ERROR;

            const pos = boss.getPosition();
            const skillService = serviceManager.getSkillService();
            const players = game.getPlayerBattleStates().stream()
                .filter(p => p != null && p.getCurrentHealth().get() > 0)
                .toArray();
            if (players.length === 0) return PhaseUpdateResult.CONTINUE;

            const castSkill = (id, key, cd, all = false) => {
                const last = abyss.bossSkillTimers.get(key) || 0;
                if (now - last >= cd) {
                    const skill = skillService.findSkillById(id);
                    if (skill) {
                        if (all) {
                            for (let p of players) {
                                const packet = new S2CMatchplayUseSkill(pos, p.getPosition(), id - 1, Math.floor(Math.random() * 127), 0, 0, 0);
                                gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);
                            }
                        } else {
                            const target = players[Math.floor(Math.random() * players.length)];
                            const packet = new S2CMatchplayUseSkill(pos, target.getPosition(), id - 1, Math.floor(Math.random() * 127), 0, 0, 0);
                            gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);
                        }
                    }
                    abyss.bossSkillTimers.set(key, now);
                }
            };

            if (!abyss.rebirthUsed) {
                for (let g of game.getGuardianBattleStates()) {
                    if (g.isBoss() || g.getCurrentHealth().get() > 0) continue;

                    const posG = g.getPosition();
                    if (abyss.resurrectedGuardians.has(posG) || abyss.scheduledRebirthGuardians.has(posG)) continue;

                    abyss.scheduledRebirthGuardians.add(posG);

                    const rebirth = skillService.findSkillById(29);
                    if (rebirth) {
                        g.getCurrentHealth().set(g.getMaxHealth());

                        const packet = new S2CMatchplayUseSkill(pos, posG, rebirth.getId() - 1, Math.floor(Math.random() * 127), 0, 0, 0);
                        const dmgPacket = new S2CMatchplayDealDamage(posG, g.getCurrentHealth().get(), rebirth.getTargeting(), rebirth.getId(), 0, 0);

                        abyss.resurrectedGuardians.add(posG);

                        const event = eventHandler.createRunnableEvent(function () {
                            gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);
                            gameManager.sendPacketToAllClientsInSameGameSession(dmgPacket, connection);
                        }, 3000);
                        eventHandler.offerJS(event);
                    }
                }

                const allResurrected = game.getGuardianBattleStates().stream()
                    .filter(g => !g.isBoss())
                    .allMatch(g => abyss.resurrectedGuardians.has(g.getPosition()));
                if (allResurrected) {
                    abyss.rebirthUsed = true;
                }
            }

            castSkill(32, "Earth", 25000);
            castSkill(61, "Pillar", 16000);
            castSkill(62, "Storm", 50000);
            castSkill(65, "Laser", 60000);
            castSkill(6, "HomingBall", 30000, true);

            if (!abyss.fullHealUsed && now - abyss.timeStarted > 30000) {
                const heal = skillService.findSkillById(31);
                if (heal) {
                    boss.getCurrentHealth().set(boss.getMaxHealth());
                    const packet = new S2CMatchplayUseSkill(pos, pos, heal.getId() - 1, Math.floor(Math.random() * 127), 0, 0, 0);
                    gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);

                    let dmgPacket = new S2CMatchplayDealDamage(pos, boss.getCurrentHealth().get(), heal.getTargeting(), heal.getId(), 0, 0);
                    gameManager.sendPacketToAllClientsInSameGameSession(dmgPacket, connection);

                    abyss.fullHealUsed = true;
                }
            }

            if (!abyss.dialogSent && now - abyss.timeStarted > 2500) {
                const msg = new S2CChatRoomAnswerPacket(2, "Boss", "You defy the tide? Let the sea claim you whole!");
                gameManager.sendPacketToAllClientsInSameGameSession(msg, connection);
                abyss.dialogSent = true;
            }

            const lastDot = abyss.bossSkillTimers.get("Dot") || 0;
            if (now - lastDot >= 10000) {
                const targets = [...players];
                const numTargets = Math.min(2, players.length);
                for (let i = 0; i < numTargets; i++) {
                    const idx = Math.floor(Math.random() * targets.length);
                    const player = targets.splice(idx, 1)[0];

                    let applyDoTTask = new ApplyDoTTask(connection, player, 3, 1000, 20);
                    let runnableEvent = eventHandler.createRunnableEvent(applyDoTTask, 50);
                    eventHandler.offer(runnableEvent);
                }
                abyss.bossSkillTimers.set("Dot", now);
            }

            for (let g of game.getGuardianBattleStates()) {
                if (g.isBoss() || g.getCurrentHealth().get() <= 0) continue;

                const lastCast = abyss.guardianSkillTimers.get(g.getPosition()) || 0;
                const isAggro = abyss.guardianAggroState.get(g.getPosition()) || false;
                const interval = isAggro ? abyss.aggroSkillInterval : abyss.skillInterval;
                if (now - lastCast >= interval) {
                    this.castGuardianSkill(g, isAggro, connection);
                    abyss.guardianSkillTimers.set(g.getPosition(), now);
                }
            }

            const aliveGuards = game.getGuardianBattleStates().stream()
                .filter(g => !g.isBoss() && g.getCurrentHealth().get() > 0)
                .toArray();

            if (aliveGuards.length === 1) {
                const soloPos = aliveGuards[0].getPosition();
                if (!abyss.guardianAggroState.get(soloPos)) {
                    abyss.guardianAggroState.set(soloPos, true);
                }
            }

            const allDead = game.getGuardianBattleStates().stream()
                .allMatch(g => g.getCurrentHealth().get() <= 0);

            if (allDead) {
                return PhaseUpdateResult.END_PHASE;
            }

            return PhaseUpdateResult.CONTINUE;
        } catch (e) {
            log.error("Script error in abyssal_reckoning.js:", e.message, e.stack || e);
            console.error(e);
            return PhaseUpdateResult.ERROR;
        }
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
        abyss.finished = true;
    },
    phaseTime: function () {
        return Date.now() - abyss.timeStarted;
    },
    playTime: function () {
        return 0;
    },
    hasEnded: function () {
        return abyss.finished || (this.playTime() !== 0 && this.phaseTime() > this.playTime());
    },
    getGuardianAttackLoopTime: function () {
        return -1;
    },
    onHeal: function (target, healAmount, isGuardian) {
        if (isGuardian) {
            return game.getGuardianCombatSystem().heal(target, healAmount);
        } else {
            return game.getPlayerCombatSystem().heal(target, healAmount);
        }
    },
    onDealDamage: function (attackingPlayer, targetGuardian, damage, hasAttackerDmgBuff, hasTargetDefBuff, skill) {
        return game.getGuardianCombatSystem().dealDamage(attackingPlayer, targetGuardian, damage, hasAttackerDmgBuff, hasTargetDefBuff, skill);
    },
    onDealDamageToPlayer: function (attackingGuardian, targetPlayer, damageAmount, hasAttackerDmgBuff, hasTargetDefBuff, skill) {
        return game.getGuardianCombatSystem().dealDamageToPlayer(attackingGuardian, targetPlayer, damageAmount, hasAttackerDmgBuff, hasTargetDefBuff, skill);
    },
    onDealDamageOnBallLoss: function (attackerPos, targetPos, hasAttackerWillBuff) {
        return game.getGuardianCombatSystem().dealDamageOnBallLoss(attackerPos, targetPos, hasAttackerWillBuff);
    },
    onDealDamageOnBallLossToPlayer: function (attackerPos, targetPos, hasAttackerWillBuff) {
        return game.getGuardianCombatSystem().dealDamageOnBallLossToPlayer(attackerPos, targetPos, hasAttackerWillBuff);
    }
}
