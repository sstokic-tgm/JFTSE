var S2CMatchplayUseSkill = Java.type("com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayUseSkill");
var S2CMatchplayDealDamage = Java.type("com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayDealDamage");
var S2CChatRoomAnswerPacket = Java.type("com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket");
var PhaseUpdateResult = Java.type("com.jftse.emulator.server.core.matchplay.guardian.PhaseUpdateResult");
var ApplyDoTTask = Java.type("com.jftse.emulator.server.core.task.ApplyDoTTask");

class MaelstromUnleashed {
    constructor() {
        this.timeStarted = 0;
        this.finished = false;
        this.phaseStarted = false;
        this.bossSkillTimers = new Map();
        this.rebirthUsed = false;
        this.dialogSent = false;
        this.guardianSkillTimers = new Map();
        this.guardianAggroState = new Map();
        this.skillInterval = 10000;
        this.aggroSkillInterval = 5000;
    }
}

let maelstrom = new MaelstromUnleashed();

var phase = {
    getPhaseName: function () {
        return "Maelstrom Unleashed";
    },
    start: function () {
        maelstrom.timeStarted = Date.now();
        maelstrom.phaseStarted = true;

        const guardians = game.getGuardianBattleStates();
        for (let g of guardians) {
            g.getSkills().clear();
            if (g.isBoss()) {
                maelstrom.bossSkillTimers.set("BigMeteo", Date.now());
                maelstrom.bossSkillTimers.set("SeaWave", Date.now());
                maelstrom.bossSkillTimers.set("Chaos", Date.now());
            } else {
                maelstrom.guardianSkillTimers.set(g.getPosition(), Date.now());
                maelstrom.guardianAggroState.set(g.getPosition(), false);
            }
        }
    },
    update: function (connection) {
        if (!maelstrom.phaseStarted || this.hasEnded()) return PhaseUpdateResult.CONTINUE;
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

            const lastBigMeteo = maelstrom.bossSkillTimers.get("BigMeteo") || 0;
            if (now - lastBigMeteo >= 20000) {
                const skill = skillService.findSkillById(3);
                if (skill) {
                    let packet = new S2CMatchplayUseSkill(pos, 4, skill.getId() - 1, Math.floor(Math.random() * 127), 0, 0, 0);
                    gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);
                    for (let player of players) {
                        let applyDoTTask = new ApplyDoTTask(connection, player, 5, 1000, 5);
                        let runnableEvent = eventHandler.createRunnableEvent(applyDoTTask, 50);
                        eventHandler.offer(runnableEvent);
                    }
                }
                maelstrom.bossSkillTimers.set("BigMeteo", now);
            }

            const lastSeaWave = maelstrom.bossSkillTimers.get("SeaWave") || 0;
            if (now - lastSeaWave >= 14000) {
                const skill = skillService.findSkillById(28);
                if (skill) {
                    let packet = new S2CMatchplayUseSkill(pos, 4, skill.getId() - 1, Math.floor(Math.random() * 127), 0, 0, 0);
                    gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);
                }
                maelstrom.bossSkillTimers.set("SeaWave", now);
            }

            const lastChaos = maelstrom.bossSkillTimers.get("Chaos") || 0;
            if (now - lastChaos >= 16000) {
                const skill = skillService.findSkillById(25);
                if (skill) {
                    for (let player of players) {
                        let packet = new S2CMatchplayUseSkill(pos, player.getPosition(), skill.getId() - 1, Math.floor(Math.random() * 127), 0, 0, 0);
                        gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);
                    }
                }
                maelstrom.bossSkillTimers.set("Chaos", now);
            }

            if (!maelstrom.rebirthUsed) {
                const deadGuardian = game.getGuardianBattleStates().stream()
                    .filter(g => !g.isBoss() && g.getCurrentHealth().get() <= 0)
                    .findFirst()
                    .orElse(null);

                if (deadGuardian) {
                    const rebirth = skillService.findSkillById(29);
                    if (rebirth) {
                        let newHealth = Math.floor(deadGuardian.getMaxHealth() * 0.3);
                        deadGuardian.getCurrentHealth().set(newHealth);

                        let packet = new S2CMatchplayUseSkill(pos, deadGuardian.getPosition(), rebirth.getId() - 1, Math.floor(Math.random() * 127), 0, 0, 0);
                        //gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);

                        let dmgPacket = new S2CMatchplayDealDamage(deadGuardian.getPosition(), deadGuardian.getCurrentHealth().get(), 4, rebirth.getId(), 0.0, 0.0);
                        gameManager.sendPacketToAllClientsInSameGameSession(dmgPacket, connection);

                        maelstrom.rebirthUsed = true;
                    }
                }
            }

            if (!maelstrom.dialogSent && now - maelstrom.timeStarted >= 30000) {
                const playerIds = game.getPlayerBattleStates().stream()
                    .filter(p => p != null)
                    .map(p => p.getId())
                    .toList();

                for (let playerId of playerIds) {
                    const playerConnection = gameManager.getConnectionByPlayerId(playerId);
                    if (playerConnection) {
                        let packet = new S2CChatRoomAnswerPacket(2, "Boss", "The tides heed no command!");
                        playerConnection.sendTCP(packet);
                    }
                }

                maelstrom.dialogSent = true;
            }

            for (let g of game.getGuardianBattleStates()) {
                if (g.isBoss() || g.getCurrentHealth().get() <= 0) continue;

                const lastCast = maelstrom.guardianSkillTimers.get(g.getPosition()) || 0;
                const isAggro = maelstrom.guardianAggroState.get(g.getPosition()) || false;
                const interval = isAggro ? maelstrom.aggroSkillInterval : maelstrom.skillInterval;
                if (now - lastCast >= interval) {
                    this.castGuardianSkill(g, isAggro, connection);
                    maelstrom.guardianSkillTimers.set(g.getPosition(), now);
                }
            }

            const aliveGuards = game.getGuardianBattleStates().stream()
                .filter(g => !g.isBoss() && g.getCurrentHealth().get() > 0)
                .toArray();

            if (aliveGuards.length === 1) {
                const soloPos = aliveGuards[0].getPosition();
                if (!maelstrom.guardianAggroState.get(soloPos)) {
                    maelstrom.guardianAggroState.set(soloPos, true);
                }
            }

            if ((this.phaseTime() > 120000 || boss.getCurrentHealth().get() < boss.getMaxHealth() * 0.5)) {
                return PhaseUpdateResult.NEXT_PHASE;
            }

            return PhaseUpdateResult.CONTINUE;
        } catch (e) {
            log.error("Script error in maelstrom_unleashed.js:", e.message, e.stack || e);
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
        maelstrom.finished = true;
    },
    phaseTime: function () {
        return Date.now() - maelstrom.timeStarted;
    },
    playTime: function () {
        return 0;
    },
    hasEnded: function () {
        return maelstrom.finished || (this.playTime() !== 0 && this.phaseTime() > this.playTime());
    },
    getGuardianAttackLoopTime: function (guardian) {
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
