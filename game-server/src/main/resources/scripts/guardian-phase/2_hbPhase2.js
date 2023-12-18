let S2CMatchplayDealDamage = Java.type("com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayDealDamage");
let S2CChatRoomAnswerPacket = Java.type("com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket");
let S2CMatchplayUseSkill = Java.type("com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayUseSkill");
let S2CMatchplayGiveSpecificSkill = Java.type("com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayGiveSpecificSkill");
let Skill2Guardians = Java.type("com.jftse.entities.database.model.battle.Skill2Guardians");
let Skill = Java.type("com.jftse.entities.database.model.battle.Skill");
let BattleUtils = Java.type("com.jftse.emulator.server.core.utils.BattleUtils");
let Thread = Java.type("java.lang.Thread");
let GuardianAttackTask = Java.type("com.jftse.emulator.server.core.task.GuardianAttackTask");
let BossBattleReward = Java.type("com.jftse.emulator.server.core.matchplay.BossBattleReward");
let PlayerScriptableImpl = Java.type("com.jftse.emulator.server.core.interaction.PlayerScriptableImpl");

class Phase2 {
    constructor() {
        this.timeStarted = 0;
        this.finished = false;
        this.phaseStarted = false;
        this.phaseCallback = null;
        this.isBossImmune = false;

        this.part1Finished = false;
        this.part2Finished = false;

        this.partIsTransitioning = false;

        this.revivedGuardians = [];

        //  after 2min 30s in ms
        this.enrageTimer = (2 * 60 * 1000) /*+ (30 * 1000)*/;
        this.isEnraged = false;

        this.enrageSkillIdLst = [37];

        this.bossEnteringPart1Messages = [
            "Boss's presence intensifies, signaling a battle shift!",
            "Air crackles with energy as boss readies its next move!",
            "Boss's power grows, heralding a change in battle!",
            "Dark magic swirls as the boss prepares for a fierce onslaught!",
            "Epicenter of power, the boss prepares for a decisive move!",
            "The battlefield trembles as the boss commands a new strategy!",
            "A surge of energy surrounds the boss, changing the tide of battle!",
            "Boss's aura intensifies, hinting at a formidable attack!",
        ];

        this.revivingGuardiansMessages = [
            "Boss revives fallen guardians!",
            "Guardians reborn with renewed strength!",
            "Fallen guardians return to battle!",
            "Guardians rise from ashes of defeat!",
            "Boss's magic breathes life into fallen guardians!",
            "Boss breathes life into fallen guardians, summoning them back to battle!",
            "Surge of magical energy resurrects fallen guardians, ready to fight anew!",
            "From depths of defeat, guardians rise again, fueled by boss's otherworldly power!",
        ];
    }
}

let phase2 = new Phase2();

var phase = {
    getPhaseName: function () {
        return "Phase 2";
    },
    start: function () {
        phase2.timeStarted = Date.now();
        phase2.phaseStarted = true;
    },
    update: function (connection) {
        if (!phase2.phaseStarted) {
            return;
        }

        if (this.hasEnded()) {
            return;
        }

        if (phase2.partIsTransitioning) {
            return;
        }

        let guardianBattleStates = game.getGuardianBattleStates();

        // if guardianBattleState is a boss, and if hp is at 80%
        let bossGuardian = guardianBattleStates.stream()
            .filter(function (guardianBattleState) {
                return guardianBattleState.isBoss() && guardianBattleState.getCurrentHealth().get() > 0;
            })
            .findFirst()
            .orElse(null);

        if (bossGuardian) {
            let currentHealthPercentage = (bossGuardian.getCurrentHealth().get() / bossGuardian.getMaxHealth()) * 100;
            if (currentHealthPercentage <= 80 && !phase2.part1Finished && !phase2.partIsTransitioning && !phase2.part2Finished) {
                phase2.part1Finished = true;
                phase2.isBossImmune = true;
                phase2.partIsTransitioning = true;

                let enteringPhaseMessage = phase2.bossEnteringPart1Messages[Math.floor(Math.random() * phase2.bossEnteringPart1Messages.length)];

                let packet = new S2CChatRoomAnswerPacket(2, "Server", enteringPhaseMessage);
                gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);

                for (let guardianBattleState of guardianBattleStates) {
                    if (guardianBattleState.getCurrentHealth().get() < 1 && !guardianBattleState.isBoss()) {
                        guardianBattleState.getCurrentHealth().set(guardianBattleState.getMaxHealth());
                        phase2.revivedGuardians.push(guardianBattleState);
                        break;
                    }
                }

                threadManager.schedule(function () {
                    let guardianBattleState = phase2.revivedGuardians.shift();
                    let healDamage = new S2CMatchplayDealDamage(guardianBattleState.getPosition(), guardianBattleState.getCurrentHealth().get(), 3, 29, 0, 0);
                    let healUseSkill = new S2CMatchplayUseSkill(bossGuardian.getPosition(), guardianBattleState.getPosition(), 28, Math.floor(Math.random() * 127), 0, 0, 0);
                    gameManager.sendPacketToAllClientsInSameGameSession(healDamage, connection);
                    gameManager.sendPacketToAllClientsInSameGameSession(healUseSkill, connection);

                    let guardAttackTask = new GuardianAttackTask(connection, guardianBattleState);
                    let runnableEvent = gameManager.getEventHandler().createRunnableEvent(guardAttackTask, 100);
                    gameManager.getEventHandler().push(runnableEvent);

                    let reviveMessage = phase2.revivingGuardiansMessages[Math.floor(Math.random() * phase2.revivingGuardiansMessages.length)];
                    packet = new S2CChatRoomAnswerPacket(2, "Server", reviveMessage);
                    gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);

                    phase2.partIsTransitioning = false;
                }, 2 * 1000); // 5 seconds

            } else if (currentHealthPercentage <= 45 && !phase2.part2Finished && !phase2.partIsTransitioning) {
                phase2.part1Finished = true;
                phase2.part2Finished = true;
                phase2.isBossImmune = true;
                phase2.partIsTransitioning = true;

                let enteringPhaseMessage = phase2.bossEnteringPart1Messages[Math.floor(Math.random() * phase2.bossEnteringPart1Messages.length)];

                let packet = new S2CChatRoomAnswerPacket(2, "Server", enteringPhaseMessage);
                gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);

                for (let guardianBattleState of guardianBattleStates) {
                    if (guardianBattleState.getCurrentHealth().get() < 1) {
                        guardianBattleState.getCurrentHealth().set(guardianBattleState.getMaxHealth());
                        phase2.revivedGuardians.push(guardianBattleState);
                        break;
                    }
                }

                threadManager.schedule(function () {
                    let guardianBattleState = phase2.revivedGuardians.shift();
                    let healDamage = new S2CMatchplayDealDamage(guardianBattleState.getPosition(), guardianBattleState.getCurrentHealth().get(), 3, 29, 0, 0);
                    let healUseSkill = new S2CMatchplayUseSkill(bossGuardian.getPosition(), guardianBattleState.getPosition(), 28, Math.floor(Math.random() * 127), 0, 0, 0);
                    gameManager.sendPacketToAllClientsInSameGameSession(healDamage, connection);
                    gameManager.sendPacketToAllClientsInSameGameSession(healUseSkill, connection);

                    let guardAttackTask = new GuardianAttackTask(connection, guardianBattleState);
                    let runnableEvent = gameManager.getEventHandler().createRunnableEvent(guardAttackTask, 100);
                    gameManager.getEventHandler().push(runnableEvent);

                    let reviveMessage = phase2.revivingGuardiansMessages[Math.floor(Math.random() * phase2.revivingGuardiansMessages.length)];
                    packet = new S2CChatRoomAnswerPacket(2, "Server", reviveMessage);
                    gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);

                    phase2.partIsTransitioning = false;
                }, 2 * 1000); // 5 seconds
            }
        }

        if (phase2.partIsTransitioning) {
            return;
        }

        let allGuardiansDead = guardianBattleStates.stream().allMatch(function (guardianBattleState) {
            return guardianBattleState.getCurrentHealth().get() < 1;
        });

        let nonBossGuardiansAllDead = guardianBattleStates.stream()
            .filter(function (guardianBattleState) {
                return !guardianBattleState.isBoss();
            })
            .allMatch(function (guardianBattleState) {
                return guardianBattleState.getCurrentHealth().get() < 1;
            });

        if (nonBossGuardiansAllDead && phase2.part1Finished) {
            phase2.isBossImmune = false;
        } else if (nonBossGuardiansAllDead && phase2.part2Finished) {
            phase2.isBossImmune = false;
        }

        if (!phase2.isEnraged && this.phaseTime() > phase2.enrageTimer) {
            if (bossGuardian) {
                let packet = new S2CChatRoomAnswerPacket(2, "Server", "Ohh no! Boss is enraged! Watch out!");
                gameManager.sendPacketToAllClientsInSameGameSession(packet, connection);

                bossGuardian.setSta(170);
                bossGuardian.setStr(250);
                bossGuardian.setDex(100);
                bossGuardian.setWill(120);

                let skills = bossGuardian.getSkills();
                skills.clear();

                for (let skillId of phase2.enrageSkillIdLst) {
                    let skill = serviceManager.getSkillService().findSkillById(skillId - 1);
                    if (skill) {
                        let skill2Guardian = new Skill2Guardians();
                        skill2Guardian.setSkill(skill);
                        skill2Guardian.setBtItemID(bossGuardian.getBtItemId());
                        skill2Guardian.setChance(100.0);
                        skills.add(skill2Guardian);
                    }
                }

                phase2.isEnraged = true;
            }
        }

        if (allGuardiansDead) {
            if (phase2.phaseCallback) {
                phase2.phaseCallback.onPhaseEnd(connection);
            }
        }
    },
    end: function () {
        phase2.finished = true;
    },
    phaseTime: function () {
        return Date.now() - phase2.timeStarted;
    },
    playTime: function () {
        return 0;
    },
    hasEnded: function () {
        return (phase2.finished) || (this.playTime() !== 0 && this.phaseTime() > this.playTime());
    },
    setPhaseCallback: function (pc) {
        phase2.phaseCallback = pc;
    },
    getGuardianAttackLoopTime: function (guardian) {
        let attackLoopTime = -1;
        if (guardian.isBoss()) {
            attackLoopTime = !phase2.isEnraged ? 4 * 1000 : 1000;
        } else {
            // 6 - 12 seconds
            attackLoopTime = Math.floor(Math.random() * 7 + 6) * 1000;
        }
        return attackLoopTime;
    },
    onHeal: function (targetGuardian, healAmount) {
        return game.getGuardianCombatSystem().heal(targetGuardian, healAmount);
    },
    onDealDamage: function (attackingPlayer, targetGuardian, damage, hasAttackerDmgBuff, hasTargetDefBuff) {
        let targetGuardianState = game.getGuardianBattleStateByPosition(targetGuardian);
        if (targetGuardianState) {
            if (phase2.isBossImmune && targetGuardianState.isBoss()) {
                return targetGuardianState.getCurrentHealth().get();
            }
        }
        return game.getGuardianCombatSystem().dealDamage(attackingPlayer, targetGuardian, damage, hasAttackerDmgBuff, hasTargetDefBuff);
    },
    onDealDamageToPlayer: function (attackingGuardian, targetPlayer, damageAmount, hasAttackerDmgBuff, hasTargetDefBuff) {
        return game.getGuardianCombatSystem().dealDamageToPlayer(attackingGuardian, targetPlayer, damageAmount, hasAttackerDmgBuff, hasTargetDefBuff);
    },
    onDealDamageOnBallLoss: function (attackerPos, targetPos, hasAttackerWillBuff) {
        let targetGuardianState = game.getGuardianBattleStateByPosition(targetPos);
        if (targetGuardianState) {
            if (phase2.isBossImmune && targetGuardianState.isBoss()) {
                return targetGuardianState.getCurrentHealth().get();
            }
        }
        return game.getGuardianCombatSystem().dealDamageOnBallLoss(attackerPos, targetPos, hasAttackerWillBuff);
    },
    onDealDamageOnBallLossToPlayer: function (attackerPos, targetPos, hasAttackerWillBuff) {
        return game.getGuardianCombatSystem().dealDamageOnBallLossToPlayer(attackerPos, targetPos, hasAttackerWillBuff);
    }
}