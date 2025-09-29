var S2CMatchplayUseSkill = Java.type("com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayUseSkill");
var S2CMatchplayDealDamage = Java.type("com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayDealDamage");
var S2CChatRoomAnswerPacket = Java.type("com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket");
var PhaseUpdateResult = Java.type("com.jftse.emulator.server.core.matchplay.guardian.PhaseUpdateResult");

class LeviathanWill {
    constructor() {
        this.timeStarted = 0;
        this.finished = false;
        this.phaseStarted = false;
        this.bossSkillTimers = new Map();
        this.silenceCast = false;
        this.healingMultiplier = 0.35;
        this.healedOnce = false;
        this.rebirthUsed = false;
        this.dialogSent = false;
        this.guardianSkillTimers = new Map();
        this.guardianAggroState = new Map();
        this.skillInterval = 10000;
        this.aggroSkillInterval = 5000;
    }
}

let leviathan = new LeviathanWill();

var phase = {
    getPhaseName: function () {
        return "Leviathan's Will";
    },
    start: function () {
        leviathan.timeStarted = Date.now();
        leviathan.phaseStarted = true;

        const guardians = game.getGuardianBattleStates();
        for (let g of guardians) {
            g.getSkills().clear();
            if (g.isBoss()) {
                leviathan.bossSkillTimers.set("MeteoBall", Date.now());
                leviathan.bossSkillTimers.set("Inferno", Date.now());
                leviathan.bossSkillTimers.set("MegaFireBall", Date.now());
                leviathan.bossSkillTimers.set("Blizzard", Date.now());
            } else {
                leviathan.guardianSkillTimers.set(g.getPosition(), Date.now());
                leviathan.guardianAggroState.set(g.getPosition(), false);
            }
        }
    },
    update: function (connection) {
        if (!leviathan.phaseStarted || this.hasEnded()) return PhaseUpdateResult.CONTINUE;

        try {
            const now = Date.now();
            const boss = game.getGuardianBattleStates().stream()
                .filter(g => g.isBoss())
                .findFirst()
                .orElse(null);
            if (!boss) return PhaseUpdateResult.ERROR;

            const pos = boss.getPosition();
            const skillService = serviceManager.getSkillService();
            const players = game.getPlayerBattleStates().stream()
                .filter(p => p != null && p.getPosition() < 4 && p.getCurrentHealth().get() > 0)
                .toArray();
            if (players.length === 0) return PhaseUpdateResult.CONTINUE;

            const castSkill = (id, cooldownKey, cooldown, targetAll = false) => {
                const lastUsed = leviathan.bossSkillTimers.get(cooldownKey) || 0;
                if (now - lastUsed >= cooldown) {
                    const skill = skillService.findSkillById(id);
                    if (skill) {
                        if (targetAll) {
                            for (let p of players) {
                                let packet = new S2CMatchplayUseSkill(pos, p.getPosition(), id - 1, Math.floor(Math.random() * 127), 0, 0, 0);
                                gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);
                            }
                        } else {
                            let target = players[Math.floor(Math.random() * players.length)];
                            let packet = new S2CMatchplayUseSkill(pos, target.getPosition(), id - 1, Math.floor(Math.random() * 127), 0, 0, 0);
                            gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);
                        }
                    }
                    leviathan.bossSkillTimers.set(cooldownKey, now);
                }
            };

            castSkill(37, "MeteoBall", 20000);
            castSkill(35, "Inferno", 15000);
            castSkill(26, "MegaFireBall", 18000, true);
            castSkill(13, "Blizzard", 25000);

            // Silence all players once after 90s
            if (!leviathan.silenceCast && now - leviathan.timeStarted > 90000) {
                const silence = skillService.findSkillById(57);
                if (silence) {
                    let target = players[Math.floor(Math.random() * players.length)];
                    let packet = new S2CMatchplayUseSkill(
                        pos,
                        target.getPosition(),
                        silence.getId() - 1,
                        Math.floor(Math.random() * 127),
                        0, 0, 0
                    );
                    gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);
                    leviathan.silenceCast = true;
                }
            }

            const deadGuardian = game.getGuardianBattleStates().stream()
                .filter(g => !g.isBoss() && g.getCurrentHealth().get() <= 0)
                .findFirst()
                .orElse(null);

            if (deadGuardian && !leviathan.rebirthUsed && Math.random() < 0.35) {
                const rebirth = skillService.findSkillById(29);
                if (rebirth) {
                    deadGuardian.getCurrentHealth().set(Math.floor(deadGuardian.getMaxHealth() * 0.3));
                    let packet = new S2CMatchplayUseSkill(pos, deadGuardian.getPosition(), rebirth.getId() - 1, Math.floor(Math.random() * 127), 0, 0, 0);
                    gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);

                    let dmgPacket = new S2CMatchplayDealDamage(deadGuardian.getPosition(), deadGuardian.getCurrentHealth().get(), rebirth.getTargeting(), rebirth.getId(), 0, 0);
                    gameManager.sendPacketToAllClientsInSameGameSession(dmgPacket, connection);

                    leviathan.rebirthUsed = true;

                    const playerIds = game.getPlayerBattleStates().stream()
                        .map(p => p.getId())
                        .toList();

                    for (let playerId of playerIds) {
                        const conn = gameManager.getConnectionByPlayerId(playerId);
                        if (conn) {
                            let msg = new S2CChatRoomAnswerPacket(2, "Boss", "The fallen rises once more, drawn by my will!");
                            conn.sendTCP(msg);
                        }
                    }
                }
            }

            if (!leviathan.healedOnce && now - leviathan.timeStarted > 85000) {
                const fullHeal = skillService.findSkillById(31);
                if (fullHeal) {
                    boss.getCurrentHealth().set(Math.floor(boss.getMaxHealth() * 0.45));
                    let packet = new S2CMatchplayUseSkill(pos, pos, fullHeal.getId() - 1, Math.floor(Math.random() * 127), 0, 0, 0);
                    gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);

                    let dmgPacket = new S2CMatchplayDealDamage(pos, boss.getCurrentHealth().get(), fullHeal.getTargeting(), fullHeal.getId(), 0, 0);
                    gameManager.sendPacketToAllClientsInSameGameSession(dmgPacket, connection);

                    leviathan.healedOnce = true;

                    for (let p of players) {
                        let msg = new S2CChatRoomAnswerPacket(2, "Boss", "The abyss grants me strength eternal!");
                        let playerConnection = gameManager.getConnectionByPlayerId(p.getId());
                        if (playerConnection) {
                            playerConnection.sendTCP(msg);
                        }
                    }
                }
            }

            if (!leviathan.dialogSent && now - leviathan.timeStarted > 60000) {
                const playerIds = game.getPlayerBattleStates().stream()
                    .filter(p => p != null)
                    .map(p => p.getId())
                    .toList();

                for (let playerId of playerIds) {
                    const playerConnection = gameManager.getConnectionByPlayerId(playerId);
                    if (playerConnection) {
                        let packet = new S2CChatRoomAnswerPacket(2, "Boss", "The Leviathan's will is unyielding!");
                        playerConnection.sendTCP(packet);
                    }
                }

                leviathan.dialogSent = true;
            }

            for (let g of game.getGuardianBattleStates()) {
                if (g.isBoss() || g.getCurrentHealth().get() <= 0) continue;

                const lastCast = leviathan.guardianSkillTimers.get(g.getPosition()) || 0;
                const isAggro = leviathan.guardianAggroState.get(g.getPosition()) || false;
                const interval = isAggro ? leviathan.aggroSkillInterval : leviathan.skillInterval;
                if (now - lastCast >= interval) {
                    this.castGuardianSkill(g, isAggro, connection);
                    leviathan.guardianSkillTimers.set(g.getPosition(), now);
                }
            }

            const aliveGuards = game.getGuardianBattleStates().stream()
                .filter(g => !g.isBoss() && g.getCurrentHealth().get() > 0)
                .toArray();

            if (aliveGuards.length === 1) {
                const soloPos = aliveGuards[0].getPosition();
                if (!leviathan.guardianAggroState.get(soloPos)) {
                    leviathan.guardianAggroState.set(soloPos, true);
                }
            }

            if ((this.phaseTime() >= 120000 || boss.getCurrentHealth().get() < boss.getMaxHealth() * 0.25)) {
                return PhaseUpdateResult.NEXT_PHASE;
            }

            return PhaseUpdateResult.CONTINUE;
        } catch (e) {
            log.error("Script error in leviathan_will.js:", e.message, e.stack || e);
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
        leviathan.finished = true;
    },
    phaseTime: function () {
        return Date.now() - leviathan.timeStarted;
    },
    playTime: function () {
        return 0;
    },
    hasEnded: function () {
        return leviathan.finished || (this.playTime() !== 0 && this.phaseTime() > this.playTime());
    },
    getGuardianAttackLoopTime: function () {
        return -1;
    },
    onHeal: function (target, healAmount, isGuardian) {
        if (isGuardian) {
            return game.getGuardianCombatSystem().heal(target, healAmount);
        } else {
            // players have a debuff that reduces healing by 65%
            const newHealAmount = Math.floor(healAmount * leviathan.healingMultiplier);
            return game.getPlayerCombatSystem().heal(target, newHealAmount);
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
