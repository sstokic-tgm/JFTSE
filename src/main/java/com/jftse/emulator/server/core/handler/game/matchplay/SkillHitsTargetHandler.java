package com.jftse.emulator.server.core.handler.game.matchplay;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.manager.ThreadManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.event.RunnableEventHandler;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomSetBossGuardiansStats;
import com.jftse.emulator.server.core.packet.packets.matchplay.*;
import com.jftse.emulator.server.core.service.BossGuardianService;
import com.jftse.emulator.server.core.service.GuardianService;
import com.jftse.emulator.server.core.service.SkillService;
import com.jftse.emulator.server.core.task.FinishGameTask;
import com.jftse.emulator.server.core.task.GuardianServeTask;
import com.jftse.emulator.server.database.model.battle.*;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Log4j2
public class SkillHitsTargetHandler extends AbstractHandler {
    private C2SMatchplaySkillHitsTarget skillHitsTarget;

    private final Random random;

    private final SkillService skillService;
    private final GuardianService guardianService;
    private final BossGuardianService bossGuardianService;

    private final RunnableEventHandler runnableEventHandler;

    public SkillHitsTargetHandler() {
        random = new Random();

        this.skillService = ServiceManager.getInstance().getSkillService();
        this.guardianService = ServiceManager.getInstance().getGuardianService();
        this.bossGuardianService = ServiceManager.getInstance().getBossGuardianService();

        runnableEventHandler = GameManager.getInstance().getRunnableEventHandler();
    }

    @Override
    public boolean process(Packet packet) {
        skillHitsTarget = new C2SMatchplaySkillHitsTarget(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActiveGameSession() == null
                || connection.getClient().getActiveRoom() == null || connection.getClient().getActivePlayer() == null)
            return;

        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGame game = gameSession.getActiveMatchplayGame();

        byte skillId = skillHitsTarget.getSkillId();

        Skill skill = skillService.findSkillById((long) skillId);

        if (skill != null && this.isUniqueSkill(skill)) {
            this.handleUniqueSkill(connection, game, skill);
            return;
        }

        boolean denyDamage = skillHitsTarget.getDamageType() == 1;
        if (skillId == 0 && !denyDamage) {
            this.handleBallLossDamage(connection, game);
        } else {
            this.handleSkillDamage(connection, skillHitsTarget.getTargetPosition(), game, skill);
        }

        if (game instanceof MatchplayBattleGame) {
            this.handleAnyTeamDead(connection, (MatchplayBattleGame) game);
        } else if (game instanceof MatchplayGuardianGame) {
            this.handleAllGuardiansDead(connection, (MatchplayGuardianGame) game);
            this.handleAllPlayersDead(connection, (MatchplayGuardianGame) game);
        }
    }

    private boolean isUniqueSkill(Skill skill) {
        int skillId = skill.getId().intValue();
        return skillId == 5 || skillId == 38;
    }

    private void handleUniqueSkill(Connection connection, MatchplayGame game, Skill skill) {
        int skillId = skill.getId().intValue();
        switch (skillId) {
            case 5 -> // Revive
                    this.handleRevivePlayer(connection, game, skill);
            case 38 -> { // Sandglass
                GameSession gameSession = connection.getClient().getActiveGameSession();
                if (gameSession != null) {
                    RunnableEvent countDownRunnable = gameSession.getCountDownRunnable();
                    if (countDownRunnable != null) {
                        countDownRunnable.setEventFireTime(countDownRunnable.getEventFireTime() + TimeUnit.SECONDS.toMillis(60));
                        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(new S2CMatchplayIncreaseBreathTimerBy60Seconds(), connection);
                    }
                }
            }
        }
    }

    private void handleRevivePlayer(Connection connection, MatchplayGame game, Skill skill) {
        boolean isBattleGame = game instanceof MatchplayBattleGame;

        PlayerBattleState playerBattleState = null;

        RoomPlayer roomPlayer = connection.getClient().getActiveRoom().getRoomPlayerList().stream()
                .filter(p -> p.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                .findFirst()
                .orElse(null);

        try {
            playerBattleState = isBattleGame ?
                    ((MatchplayBattleGame) game).getPlayerCombatSystem().reviveAnyPlayer(skill.getDamage().shortValue(), roomPlayer) :
                    ((MatchplayGuardianGame) game).getPlayerCombatSystem().reviveAnyPlayer(skill.getDamage().shortValue());
        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
            return;
        }

        if (playerBattleState != null) {
            S2CMatchplayDealDamage damageToPlayerPacket =
                    new S2CMatchplayDealDamage((short) playerBattleState.getPosition().get(), (short) playerBattleState.getCurrentHealth().get(), (short) 0, skillHitsTarget.getSkillId(), skillHitsTarget.getXKnockbackPosition(), skillHitsTarget.getYKnockbackPosition());
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(damageToPlayerPacket, connection);
        }
    }

    private void handleBallLossDamage(Connection connection, MatchplayGame game) {
        short receiverPosition = skillHitsTarget.getTargetPosition();
        short attackerPosition = skillHitsTarget.getAttackerPosition();
        boolean attackerHasWillBuff = skillHitsTarget.getAttackerBuffId() == 3;

        boolean guardianMadePoint = skillHitsTarget.getTargetPosition() < 4;

        short newHealth;
        try {
            if (game instanceof MatchplayBattleGame) {
                newHealth = ((MatchplayBattleGame) game).getPlayerCombatSystem().dealDamageOnBallLoss(attackerPosition, receiverPosition, attackerHasWillBuff);
            } else {
                if (guardianMadePoint) {
                    if (!skillHitsTarget.isApplySkillEffect()) {
                        return;
                    }

                    newHealth = ((MatchplayGuardianGame) game).getGuardianCombatSystem().dealDamageOnBallLossToPlayer(attackerPosition, receiverPosition, attackerHasWillBuff);
                } else {
                    newHealth = ((MatchplayGuardianGame) game).getGuardianCombatSystem().dealDamageOnBallLoss(attackerPosition, receiverPosition, attackerHasWillBuff);
                    if (newHealth < 1) {
                        this.increasePotsFromGuardiansDeath((MatchplayGuardianGame) game, receiverPosition);
                    }
                }
            }
        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
            return;
        }

        S2CMatchplayDealDamage damagePacket = new S2CMatchplayDealDamage(skillHitsTarget.getTargetPosition(), newHealth, (short) 0, (byte) 0, 0, 0);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(damagePacket, connection);
    }

    private void handleSkillDamage(Connection connection, short targetPosition, MatchplayGame game, Skill skill) {
        boolean denyDamage = skillHitsTarget.getDamageType() == 1;
        short attackerPosition = skillHitsTarget.getAttackerPosition();
        boolean attackerHasStrBuff = skillHitsTarget.getAttackerBuffId() == 0;
        boolean receiverHasDefBuff = skillHitsTarget.getReceiverBuffId() == 1;

        short skillDamage = skill != null ? skill.getDamage().shortValue() : -1;
        // System.out.println("attacker: " + attackerPosition + "\ntarget: " + targetPosition + "\nskill: " + (skill != null ? skill.getName() : "null") + "\nskillDamage: " + skillDamage + "\ndenyDamage: " + denyDamage + "\nisApplySkillEffect: " + skillHitsTarget.isApplySkillEffect());

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long currentTimestamp = cal.getTimeInMillis();
        if (skillDamage >= 1) { // handle double activation of skills (shields, heals)
            PlayerBattleState targetPlayer = game instanceof MatchplayBattleGame ?
                    ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                            .filter(x -> x.getPosition().get() == targetPosition)
                            .findFirst()
                            .orElse(null) :
                    ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                            .filter(x -> x.getPosition().get() == targetPosition)
                            .findFirst()
                            .orElse(null);

            if (targetPlayer != null) {
                if (targetPlayer.getLastSkillHitsTarget().containsKey(skill.getId())) {
                    long timestamp = targetPlayer.getLastSkillHitsTarget().get(skill.getId());

                    long timePassed = timestamp - currentTimestamp;
                    if (timePassed <= 50) {
                        targetPlayer.getLastSkillHitsTarget().remove(skill.getId());
                        return;
                    }
                } else {
                    targetPlayer.getLastSkillHitsTarget().put(skill.getId(), currentTimestamp);
                }
            }
        }

        short newHealth;
        if (game instanceof MatchplayBattleGame) {
            try {
                if (skillDamage > 1) {
                    newHealth = ((MatchplayBattleGame) game).getPlayerCombatSystem().heal(targetPosition, skillDamage);
                } else if (denyDamage) {
                    newHealth = ((MatchplayBattleGame) game).getPlayerCombatSystem().dealDamage(attackerPosition, targetPosition, (short) -1, false, false);
                } else if (skillDamage == 0) {
                    newHealth = ((MatchplayBattleGame) game).getPlayerCombatSystem().getPlayerCurrentHealth(targetPosition);
                } else if (!skillHitsTarget.isApplySkillEffect()) {
                    return;
                } else {
                    newHealth = ((MatchplayBattleGame) game).getPlayerCombatSystem().dealDamage(attackerPosition, targetPosition, skillDamage, attackerHasStrBuff, receiverHasDefBuff);
                }
            } catch (ValidationException ve) {
                log.warn(ve.getMessage());
                return;
            }
        } else {
            if (targetPosition < 4) {
                try {
                    if (skillDamage > 1) {
                        newHealth = ((MatchplayGuardianGame) game).getPlayerCombatSystem().heal(targetPosition, skillDamage);
                    } else if (denyDamage) {
                        newHealth = ((MatchplayGuardianGame) game).getGuardianCombatSystem().dealDamageToPlayer(attackerPosition, targetPosition, (short) -1, false, false);
                    } else if (skillDamage == 0) {
                        newHealth = ((MatchplayGuardianGame) game).getPlayerCombatSystem().getPlayerCurrentHealth(targetPosition);
                    } else if (!skillHitsTarget.isApplySkillEffect()) {
                        return;
                    } else {
                        newHealth = ((MatchplayGuardianGame) game).getGuardianCombatSystem().dealDamageToPlayer(attackerPosition, targetPosition, skillDamage, attackerHasStrBuff, receiverHasDefBuff);
                    }
                } catch (ValidationException ve) {
                    log.warn(ve.getMessage());
                    return;
                }
            } else {
                try {
                    if (skillDamage > 1) {
                        newHealth = ((MatchplayGuardianGame) game).getGuardianCombatSystem().heal(targetPosition, skillDamage);
                    } else if (denyDamage) {
                        newHealth = ((MatchplayGuardianGame) game).getGuardianCombatSystem().dealDamage(attackerPosition, targetPosition, (short) -1, false, false);
                    } else {
                        newHealth = ((MatchplayGuardianGame) game).getGuardianCombatSystem().dealDamage(attackerPosition, targetPosition, skillDamage, attackerHasStrBuff, receiverHasDefBuff);
                    }
                } catch (ValidationException ve) {
                    log.warn(ve.getMessage());
                    return;
                }

                if (newHealth < 1) {
                    this.increasePotsFromGuardiansDeath((MatchplayGuardianGame) game, targetPosition);
                }
            }
        }

        byte skillToApply = this.getSkillToApply(skill);
        S2CMatchplayDealDamage damageToPlayerPacket =
                new S2CMatchplayDealDamage(targetPosition, newHealth, (short) 0, skillToApply, skillHitsTarget.getXKnockbackPosition(), skillHitsTarget.getYKnockbackPosition());
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(damageToPlayerPacket, connection);
    }

    private void increasePotsFromGuardiansDeath(MatchplayGuardianGame game, int guardianPos) {
        GuardianBattleState guardianBattleState = game.getGuardianBattleStates().stream()
                .filter(x -> x.getPosition().get() == guardianPos)
                .findFirst()
                .orElse(null);
        if (guardianBattleState != null && !guardianBattleState.getLooted().get()) {
            if (guardianBattleState.getLooted().compareAndSet(false, true)) {
                game.getExpPot().getAndSet(game.getExpPot().get() + guardianBattleState.getExp());
                game.getGoldPot().getAndSet(game.getGoldPot().get() + guardianBattleState.getGold());
            }
        }
    }

    private byte getSkillToApply(Skill skill) {
        boolean targetHittingHimself = skillHitsTarget.getAttackerPosition() == skillHitsTarget.getTargetPosition();
        if (skill != null && skill.getId() == 64 && targetHittingHimself) {
            return 3;
        }

        if (!skillHitsTarget.isApplySkillEffect()) {
            return 3;
        }

        return skillHitsTarget.getSkillId();
    }

    private void handleAnyTeamDead(Connection connection, MatchplayBattleGame game) {
        boolean allPlayersTeamRedDead = game.getPlayerBattleStates().stream().filter(x -> game.isRedTeam(x.getPosition().get())).allMatch(x -> x.getCurrentHealth().get() < 1);
        boolean allPlayersTeamBlueDead = game.getPlayerBattleStates().stream().filter(x -> game.isBlueTeam(x.getPosition().get())).allMatch(x -> x.getCurrentHealth().get() < 1);

        if ((allPlayersTeamRedDead || allPlayersTeamBlueDead) && !game.getFinished().get()) {
            ThreadManager.getInstance().newTask(new FinishGameTask(connection));
        }
    }

    private void handleAllGuardiansDead(Connection connection, MatchplayGuardianGame game) {
        boolean hasBossGuardianStage = game.getBossGuardianStage() != null;
        boolean allGuardiansDead = game.getGuardianBattleStates().stream().allMatch(x -> x.getCurrentHealth().get() < 1);
        long timePlayingInSeconds = game.getStageTimePlayingInSeconds();
        boolean triggerBossBattle = game.getIsHardMode().get() && timePlayingInSeconds < 300 || timePlayingInSeconds < game.getGuardianStage().getBossTriggerTimerInSeconds();
        if ((hasBossGuardianStage || game.getIsHardMode().get()) && allGuardiansDead && triggerBossBattle && !game.getBossBattleActive().get()) {
            GameSession gameSession = connection.getClient().getActiveGameSession();
            gameSession.clearCountDownRunnable();

            if (!hasBossGuardianStage && game.getIsHardMode().get()) {
                GuardianStage guardianStage = game.getGuardianStage();
                guardianStage.setBossGuardian((int) (Math.random() * 7) + 1);
                game.setBossGuardianStage(guardianStage);
            }

            game.setCurrentStage(game.getBossGuardianStage());

            int activePlayingPlayersCount = game.getPlayerBattleStates().size();
            List<Byte> guardians = game.determineGuardians(game.getBossGuardianStage(), game.getGuardianLevelLimit());
            byte bossGuardianIndex = game.getBossGuardianStage().getBossGuardian().byteValue();
            game.getBossBattleActive().getAndSet(true);
            game.getGuardianBattleStates().clear();

            BossGuardian bossGuardian = this.bossGuardianService.findBossGuardianById((long) bossGuardianIndex);
            GuardianBattleState bossGuardianBattleState = game.createGuardianBattleState(false, bossGuardian, (short) 10, activePlayingPlayersCount);
            game.getGuardianBattleStates().add(bossGuardianBattleState);

            if (game.getIsHardMode().get() && !hasBossGuardianStage) {
                game.fillRemainingGuardianSlots(true, game, game.getBossGuardianStage(), guardians);
                guardians.set(2, (byte) 0);
            }

            byte guardianStartPosition = 11;
            for (int i = 0; i < guardians.stream().count(); i++) {
                int guardianId = guardians.get(i);
                if (guardianId == 0) continue;

                if (game.getRandomGuardiansMode().get()) {
                    guardianId = (int) (Math.random() * 72 + 1);
                    guardians.set(i, (byte) guardianId);
                }

                short guardianPosition = (short) (i + guardianStartPosition);
                Guardian guardian = guardianService.findGuardianById((long) guardianId);
                GuardianBattleState guardianBattleState = game.createGuardianBattleState(game.getIsHardMode().get(), guardian, guardianPosition, activePlayingPlayersCount);
                game.getGuardianBattleStates().add(guardianBattleState);
            }

            game.resetStageStartTime();

            S2CRoomSetBossGuardiansStats setBossGuardiansStats = new S2CRoomSetBossGuardiansStats(game.getGuardianBattleStates());
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(setBossGuardiansStats, connection);

            S2CMatchplaySpawnBossBattle matchplaySpawnBossBattle = new S2CMatchplaySpawnBossBattle(bossGuardianIndex, guardians.get(0), guardians.get(1));
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(matchplaySpawnBossBattle, connection);

            RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(new GuardianServeTask(connection), TimeUnit.SECONDS.toMillis(18));
            gameSession.getRunnableEvents().add(runnableEvent);
        } else if (allGuardiansDead && !game.getFinished().get()) {
            ThreadManager.getInstance().newTask(new FinishGameTask(connection, true));
        }
    }

    private void handleAllPlayersDead(Connection connection, MatchplayGuardianGame game) {
        boolean allPlayersDead = game.getPlayerBattleStates().stream().allMatch(x -> x.getCurrentHealth().get() < 1);
        if (allPlayersDead && !game.getFinished().get()) {
            ThreadManager.getInstance().newTask(new FinishGameTask(connection, false));
        }
    }
}
