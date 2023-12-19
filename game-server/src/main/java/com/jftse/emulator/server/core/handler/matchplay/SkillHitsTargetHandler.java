package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.jdbc.JdbcUtil;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomSetBossGuardiansStats;
import com.jftse.emulator.server.core.packets.matchplay.C2SMatchplaySkillHitsTarget;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayDealDamage;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayIncreaseBreathTimerBy60Seconds;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplaySpawnBossBattle;
import com.jftse.emulator.server.core.task.PlaceCrystalRandomlyTask;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.battle.*;
import com.jftse.entities.database.model.scenario.MScenarios;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.task.FinishGameTask;
import com.jftse.emulator.server.core.task.GuardianServeTask;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.BossGuardianService;
import com.jftse.server.core.service.GuardianService;
import com.jftse.server.core.service.SkillService;
import com.jftse.server.core.thread.ThreadManager;
import lombok.extern.log4j.Log4j2;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Log4j2
@PacketOperationIdentifier(PacketOperations.C2SMatchplaySkillHitsTarget)
public class SkillHitsTargetHandler extends AbstractPacketHandler {
    private C2SMatchplaySkillHitsTarget skillHitsTarget;

    private final Random random;

    private final SkillService skillService;
    private final GuardianService guardianService;
    private final BossGuardianService bossGuardianService;

    private final EventHandler eventHandler;

    private final JdbcUtil jdbcUtil;

    public SkillHitsTargetHandler() {
        random = new Random();

        this.skillService = ServiceManager.getInstance().getSkillService();
        this.guardianService = ServiceManager.getInstance().getGuardianService();
        this.bossGuardianService = ServiceManager.getInstance().getBossGuardianService();

        eventHandler = GameManager.getInstance().getEventHandler();

        jdbcUtil = ServiceManager.getInstance().getJdbcUtil();
    }

    @Override
    public boolean process(Packet packet) {
        skillHitsTarget = new C2SMatchplaySkillHitsTarget(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getActiveGameSession() == null
                || ftClient.getActiveRoom() == null || ftClient.getPlayer() == null)
            return;

        GameSession gameSession = ftClient.getActiveGameSession();
        if (gameSession == null)
            return;

        MatchplayGame game = gameSession.getMatchplayGame();
        if (game == null)
            return;

        byte skillId = skillHitsTarget.getSkillId();

        Skill skill = skillService.findSkillById((long) skillId);

        if (skill != null && this.isUniqueSkill(skill)) {
            this.handleUniqueSkill(ftClient.getConnection(), game, skill);
            return;
        }

        boolean denyDamage = skillHitsTarget.getDamageType() == 1;
        if (skillId == 0 && !denyDamage) {
            if (!this.handleBallLossDamage(ftClient.getConnection(), game))
                return;
        } else {
            if (!this.handleSkillDamage(ftClient.getConnection(), skillHitsTarget.getTargetPosition(), game, skill))
                return;
        }

        if (game instanceof MatchplayBattleGame) {
            this.handleAnyTeamDead(ftClient.getConnection(), (MatchplayBattleGame) game);
        } else {
            this.handleAllGuardiansDead(ftClient.getConnection(), (MatchplayGuardianGame) game);
            this.handleAllPlayersDead(ftClient.getConnection(), (MatchplayGuardianGame) game);
        }
    }

    private boolean isUniqueSkill(Skill skill) {
        int skillId = skill.getId().intValue();
        return skillId == 5 || skillId == 38;
    }

    private void handleUniqueSkill(FTConnection connection, MatchplayGame game, Skill skill) {
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

    private void handleRevivePlayer(FTConnection connection, MatchplayGame game, Skill skill) {
        boolean isBattleGame = game instanceof MatchplayBattleGame;

        PlayerBattleState playerBattleState = null;

        RoomPlayer roomPlayer = connection.getClient().getRoomPlayer();
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
                    new S2CMatchplayDealDamage((short) playerBattleState.getPosition(), (short) playerBattleState.getCurrentHealth().get(), skill.getTargeting().shortValue(), skill.getId().byteValue(), skillHitsTarget.getXKnockbackPosition(), skillHitsTarget.getYKnockbackPosition());
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(damageToPlayerPacket, connection);
        }
    }

    private boolean handleBallLossDamage(FTConnection connection, MatchplayGame game) {
        short receiverPosition = skillHitsTarget.getTargetPosition();
        short attackerPosition = skillHitsTarget.getAttackerPosition();
        boolean attackerHasWillBuff = skillHitsTarget.getAttackerBuffId() == 3;

        boolean guardianMadePoint = skillHitsTarget.getTargetPosition() < 4;

        short newHealth;
        try {
            if (game instanceof MatchplayBattleGame battleGame) {
                newHealth = battleGame.getPlayerCombatSystem().dealDamageOnBallLoss(attackerPosition, receiverPosition, attackerHasWillBuff);
            } else {
                MatchplayGuardianGame guardianGame = (MatchplayGuardianGame) game;
                final boolean isAdvancedBossGuardianModeActive = guardianGame.isAdvancedBossGuardianMode() && guardianGame.getPhaseManager().getIsRunning().get();

                if (guardianMadePoint) {
                    if (!skillHitsTarget.isApplySkillEffect()) {
                        return false;
                    }

                    if (isAdvancedBossGuardianModeActive) {
                        newHealth = (short) guardianGame.getPhaseManager().onDealDamageOnBallLossToPlayer(attackerPosition, receiverPosition, attackerHasWillBuff);
                    } else {
                        newHealth = guardianGame.getGuardianCombatSystem().dealDamageOnBallLossToPlayer(attackerPosition, receiverPosition, attackerHasWillBuff);
                    }
                } else {
                    if (isAdvancedBossGuardianModeActive) {
                        newHealth = (short) guardianGame.getPhaseManager().onDealDamageOnBallLoss(attackerPosition, receiverPosition, attackerHasWillBuff);
                    } else {
                        newHealth = guardianGame.getGuardianCombatSystem().dealDamageOnBallLoss(attackerPosition, receiverPosition, attackerHasWillBuff);
                    }
                    if (newHealth < 1) {
                        this.increasePotsFromGuardiansDeath((MatchplayGuardianGame) game, receiverPosition);
                    }
                }
            }
        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
            return false;
        }

        S2CMatchplayDealDamage damagePacket = new S2CMatchplayDealDamage(skillHitsTarget.getTargetPosition(), newHealth, (short) 0, (byte) 0, 0, 0);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(damagePacket, connection);
        return true;
    }

    private boolean handleSkillDamage(FTConnection connection, short targetPosition, MatchplayGame game, Skill skill) {
        boolean denyDamage = skillHitsTarget.getDamageType() == 1;
        short attackerPosition = skillHitsTarget.getAttackerPosition();
        boolean attackerHasStrBuff = skillHitsTarget.getAttackerBuffId() == 0;
        boolean receiverHasDefBuff = skillHitsTarget.getReceiverBuffId() == 1;

        short skillDamage = skill != null ? skill.getDamage().shortValue() : -1;

        short newHealth;
        if (game instanceof MatchplayBattleGame battleGame) {
            try {
                PlayerBattleState playerBattleState = battleGame.getPlayerBattleStates().stream()
                        .filter(state -> state.getPosition() == targetPosition)
                        .findFirst()
                        .orElse(null);
                if (playerBattleState != null && playerBattleState.getCurrentHealth().get() < 1)
                    return false;

                if (skillDamage > 1) {
                    newHealth = battleGame.getPlayerCombatSystem().heal(targetPosition, skillDamage);
                } else if (denyDamage) {
                    newHealth = battleGame.getPlayerCombatSystem().dealDamage(attackerPosition, targetPosition, (short) -1, false, false);
                } else if (skillDamage == 0) {
                    newHealth = battleGame.getPlayerCombatSystem().getPlayerCurrentHealth(targetPosition);
                } else if (!skillHitsTarget.isApplySkillEffect()) {
                    return false;
                } else {
                    newHealth = battleGame.getPlayerCombatSystem().dealDamage(attackerPosition, targetPosition, skillDamage, attackerHasStrBuff, receiverHasDefBuff);
                }
            } catch (ValidationException ve) {
                log.warn(ve.getMessage());
                return false;
            }
        } else {
            MatchplayGuardianGame guardianGame = (MatchplayGuardianGame) game;
            final boolean isAdvancedBossGuardianModeActive = guardianGame.isAdvancedBossGuardianMode() && guardianGame.getPhaseManager().getIsRunning().get();

            if (targetPosition < 4) {
                try {
                    PlayerBattleState playerBattleState = guardianGame.getPlayerBattleStates().stream()
                            .filter(state -> state.getPosition() == targetPosition)
                            .findFirst()
                            .orElse(null);
                    if (playerBattleState != null && playerBattleState.getCurrentHealth().get() < 1)
                        return false;

                    if (skillDamage > 1) {
                        newHealth = guardianGame.getPlayerCombatSystem().heal(targetPosition, skillDamage);
                    } else if (denyDamage) {
                        if (isAdvancedBossGuardianModeActive) {
                            newHealth = (short) guardianGame.getPhaseManager().onDealDamageToPlayer(attackerPosition, targetPosition, (short) -1, false, false);
                        } else {
                            newHealth = guardianGame.getGuardianCombatSystem().dealDamageToPlayer(attackerPosition, targetPosition, (short) -1, false, false);
                        }
                    } else if (skillDamage == 0) {
                        newHealth = guardianGame.getPlayerCombatSystem().getPlayerCurrentHealth(targetPosition);
                    } else if (!skillHitsTarget.isApplySkillEffect()) {
                        return false;
                    } else {
                        if (isAdvancedBossGuardianModeActive) {
                            newHealth = (short) guardianGame.getPhaseManager().onDealDamageToPlayer(attackerPosition, targetPosition, skillDamage, attackerHasStrBuff, receiverHasDefBuff);
                        } else {
                            newHealth = guardianGame.getGuardianCombatSystem().dealDamageToPlayer(attackerPosition, targetPosition, skillDamage, attackerHasStrBuff, receiverHasDefBuff);
                        }
                    }
                } catch (ValidationException ve) {
                    log.warn(ve.getMessage());
                    return false;
                }
            } else {
                try {
                    GuardianBattleState guardianBattleState = guardianGame.getGuardianBattleStates().stream()
                            .filter(state -> state.getPosition() == targetPosition)
                            .findFirst()
                            .orElse(null);
                    if (guardianBattleState != null && guardianBattleState.getCurrentHealth().get() < 1)
                        return false;

                    if (isAdvancedBossGuardianModeActive) {
                        if (skillDamage > 1) {
                            newHealth = (short) guardianGame.getPhaseManager().onHeal(targetPosition, skillDamage);
                        } else if (denyDamage) {
                            newHealth = (short) guardianGame.getPhaseManager().onDealDamage(attackerPosition, targetPosition, (short) -1, false, false);
                        } else {
                            newHealth = (short) guardianGame.getPhaseManager().onDealDamage(attackerPosition, targetPosition, skillDamage, attackerHasStrBuff, receiverHasDefBuff);
                        }
                    } else {
                        if (skillDamage > 1) {
                            newHealth = guardianGame.getGuardianCombatSystem().heal(targetPosition, skillDamage);
                        } else if (denyDamage) {
                            newHealth = guardianGame.getGuardianCombatSystem().dealDamage(attackerPosition, targetPosition, (short) -1, false, false);
                        } else {
                            newHealth = guardianGame.getGuardianCombatSystem().dealDamage(attackerPosition, targetPosition, skillDamage, attackerHasStrBuff, receiverHasDefBuff);
                        }
                    }
                } catch (ValidationException ve) {
                    log.warn(ve.getMessage());
                    return false;
                }

                if (newHealth < 1) {
                    this.increasePotsFromGuardiansDeath((MatchplayGuardianGame) game, targetPosition);
                }
            }
        }

        Skill skillToApply = this.getSkillToApply(skill);
        S2CMatchplayDealDamage damageToPlayerPacket =
                new S2CMatchplayDealDamage(targetPosition, newHealth, skillToApply.getTargeting().shortValue(), skillToApply.getId().byteValue(), skillHitsTarget.getXKnockbackPosition(), skillHitsTarget.getYKnockbackPosition());
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(damageToPlayerPacket, connection);
        return true;
    }

    private void increasePotsFromGuardiansDeath(MatchplayGuardianGame game, int guardianPos) {
        GuardianBattleState guardianBattleState = game.getGuardianBattleStates().stream()
                .filter(x -> x.getPosition() == guardianPos)
                .findFirst()
                .orElse(null);
        if (guardianBattleState != null) {
            if (guardianBattleState.getLooted().compareAndSet(false, true)) {
                handlePotsIncrease(game, guardianBattleState);
            }
        }
    }

    private void handlePotsIncrease(MatchplayGuardianGame game, GuardianBattleState guardianBattleState) {
        jdbcUtil.execute(em -> {
            String qlGetGuardianByMapAndScenario =
                    "SELECT g2m FROM Guardian2Maps g2m " +
                    "WHERE g2m.map.id = :mapId AND g2m.status.id = 1 AND g2m.scenario.id = :scenarioId ";
            if (guardianBattleState.isBoss())
                qlGetGuardianByMapAndScenario += "AND g2m.bossGuardian.id = :guardianId ";
            else
                qlGetGuardianByMapAndScenario += "AND g2m.guardian.id = :guardianId ";

            TypedQuery<Guardian2Maps> query = em.createQuery(qlGetGuardianByMapAndScenario, Guardian2Maps.class);
            query.setParameter("guardianId", (long) guardianBattleState.getId());
            query.setParameter("mapId", game.getMap().getId());
            query.setParameter("scenarioId", game.getScenario().getId());
            List<Guardian2Maps> guardian2MapsList = query.getResultList();

            Guardian2Maps guardian = guardian2MapsList.isEmpty() ? null : guardian2MapsList.get(0);
            if (guardian != null) {
                String qlGetMultiplierByGuardAndRole =
                        "SELECT sgm FROM SRelationships sr " +
                        "LEFT JOIN FETCH SGuardianMultiplier sgm ON sgm.id = sr.id_f " +
                        "WHERE sr.id_t = :guardId AND sr.status.id = 1 AND sr.relationship.id IN (4, 5) AND sr.role.id = :roleId";

                TypedQuery<SGuardianMultiplier> queryMultiplier = em.createQuery(qlGetMultiplierByGuardAndRole, SGuardianMultiplier.class);
                queryMultiplier.setParameter("guardId", guardian.getId());
                queryMultiplier.setParameter("roleId", 2L);
                final List<SGuardianMultiplier> expMultipliers = queryMultiplier.getResultList();

                queryMultiplier = em.createQuery(qlGetMultiplierByGuardAndRole, SGuardianMultiplier.class);
                queryMultiplier.setParameter("guardId", guardian.getId());
                queryMultiplier.setParameter("roleId", 3L);
                final List<SGuardianMultiplier> goldMultipliers = queryMultiplier.getResultList();

                SGuardianMultiplier sExpMultiplier = expMultipliers.isEmpty() ? null : expMultipliers.get(0);
                SGuardianMultiplier sGoldMultiplier = goldMultipliers.isEmpty() ? null : goldMultipliers.get(0);

                double expMultiplier = sExpMultiplier == null ? 1 : sExpMultiplier.getMultiplier();
                double goldMultiplier = sGoldMultiplier == null ? 1 : sGoldMultiplier.getMultiplier();
                game.getExpPot().getAndAdd((int) (guardianBattleState.getExp() * expMultiplier));
                game.getGoldPot().getAndAdd((int) (guardianBattleState.getGold() * goldMultiplier));
            } else {
                String qlGetGuardianByGuardian =
                        "SELECT g2m FROM Guardian2Maps g2m " +
                        "WHERE g2m.status.id = 1 ";
                if (guardianBattleState.isBoss())
                    qlGetGuardianByGuardian += "AND g2m.bossGuardian.id = :guardianId ";
                else
                    qlGetGuardianByGuardian += "AND g2m.guardian.id = :guardianId ";

                query = em.createQuery(qlGetGuardianByGuardian, Guardian2Maps.class);
                query.setParameter("guardianId", (long) guardianBattleState.getId());

                guardian2MapsList.clear();
                guardian2MapsList = query.getResultList();

                guardian = guardian2MapsList.isEmpty() ? null : guardian2MapsList.get(0);
                if (guardian != null) {
                    String qlGetMultiplierByGuardAndRole =
                            "SELECT sgm FROM SRelationships sr " +
                            "LEFT JOIN FETCH SGuardianMultiplier sgm ON sgm.id = sr.id_f " +
                            "WHERE sr.id_t = :guardId AND sr.status.id = 1 AND sr.relationship.id IN (4, 5) AND sr.role.id = :roleId";

                    TypedQuery<SGuardianMultiplier> queryMultiplier = em.createQuery(qlGetMultiplierByGuardAndRole, SGuardianMultiplier.class);
                    queryMultiplier.setParameter("guardId", guardian.getId());
                    queryMultiplier.setParameter("roleId", 2L);
                    final List<SGuardianMultiplier> expMultipliers = queryMultiplier.getResultList();

                    queryMultiplier = em.createQuery(qlGetMultiplierByGuardAndRole, SGuardianMultiplier.class);
                    queryMultiplier.setParameter("guardId", guardian.getId());
                    queryMultiplier.setParameter("roleId", 3L);
                    final List<SGuardianMultiplier> goldMultipliers = queryMultiplier.getResultList();

                    SGuardianMultiplier sExpMultiplier = expMultipliers.isEmpty() ? null : expMultipliers.get(0);
                    SGuardianMultiplier sGoldMultiplier = goldMultipliers.isEmpty() ? null : goldMultipliers.get(0);

                    double expMultiplier = sExpMultiplier == null ? 1 : sExpMultiplier.getMultiplier();
                    double goldMultiplier = sGoldMultiplier == null ? 1 : sGoldMultiplier.getMultiplier();
                    game.getExpPot().getAndAdd((int) (guardianBattleState.getExp() * expMultiplier));
                    game.getGoldPot().getAndAdd((int) (guardianBattleState.getGold() * goldMultiplier));
                }
            }
        });
    }

    private Skill getSkillToApply(Skill skill) {
        boolean targetHittingHimself = skillHitsTarget.getAttackerPosition() == skillHitsTarget.getTargetPosition();
        if (skill != null && skill.getId() == 64 && targetHittingHimself) {
            log.debug("skill.getId() == 64 && targetHittingHimself return 3");
            return skillService.findSkillById(3L);
        }

        if (!skillHitsTarget.isApplySkillEffect()) {
            log.debug("!skillHitsTarget.isApplySkillEffect() return 3");
            return skillService.findSkillById(3L);
        }

        Skill skillToApply = skillService.findSkillById((long) skillHitsTarget.getSkillId());
        if (skillToApply == null && skillHitsTarget.getSkillId() == 0) {
            skillToApply = new Skill();
            skillToApply.setId(0L);
            skillToApply.setTargeting(0);
        }
        return skillToApply;
    }

    private void handleAnyTeamDead(FTConnection connection, MatchplayBattleGame game) {
        boolean allPlayersTeamRedDead = game.getPlayerBattleStates().stream().filter(x -> game.isRedTeam(x.getPosition())).allMatch(x -> x.getCurrentHealth().get() < 1);
        boolean allPlayersTeamBlueDead = game.getPlayerBattleStates().stream().filter(x -> game.isBlueTeam(x.getPosition())).allMatch(x -> x.getCurrentHealth().get() < 1);

        if ((allPlayersTeamRedDead || allPlayersTeamBlueDead) && !game.getFinished().get()) {
            ThreadManager.getInstance().newTask(new FinishGameTask(connection));
        }
    }

    private void handleAllGuardiansDead(FTConnection connection, MatchplayGuardianGame game) {
        final boolean isHardMode = game.getIsHardMode().get();
        boolean stageChangingToBoss = game.getStageChangingToBoss().get();
        final boolean hasBossGuardianStage = game.getMap().getIsBossStage();
        boolean allGuardiansDead = game.getGuardianBattleStates().stream().allMatch(x -> x.getCurrentHealth().get() < 1);
        final long timePlayingInSeconds = game.getStageTimePlayingInSeconds();
        final boolean triggerBossBattle = game.getIsHardMode().get() && timePlayingInSeconds < 300 || (game.getMap().getTriggerBossTime() != null && timePlayingInSeconds < TimeUnit.MINUTES.toSeconds(game.getMap().getTriggerBossTime()));
        final boolean isBossBattleActive = game.getBossBattleActive().get();

        final boolean canEnterBossBattle = (hasBossGuardianStage || isHardMode) && allGuardiansDead && triggerBossBattle && !isBossBattleActive && !stageChangingToBoss;
        if (canEnterBossBattle) {
            if (!game.getStageChangingToBoss().compareAndSet(false, true))
                return;

            if (!game.getBossBattleActive().compareAndSet(false, true))
                return;

            GameSession gameSession = connection.getClient().getActiveGameSession();
            gameSession.clearCountDownRunnable();
            gameSession.getFireables().forEach(f -> f.setCancelled(true));
            gameSession.getFireables().clear();

            boolean isAdvancedBossGuardianMode = false;
            MScenarios.GameMode gameMode;

            // mons only
            if (Arrays.asList(8L, 9L).contains(game.getMap().getId()) && !isHardMode) {
                gameMode = MScenarios.GameMode.BOSS_BATTLE_V2;
                isAdvancedBossGuardianMode = true;
            } else {
                gameMode = MScenarios.GameMode.BOSS_BATTLE;
            }

            MScenarios bossBattleScenario = ServiceManager.getInstance().getScenarioService().getDefaultScenarioByMapAndGameMode(game.getMap().getId(), gameMode);
            game.setScenario(bossBattleScenario);

            final Guardian2Maps[] bossGuardianArr = { null };

            if (!hasBossGuardianStage && isHardMode) {
                game.getGuardiansInStage().removeIf(g2m -> g2m.getSide() == Guardian2Maps.Side.MIDDLE);

                jdbcUtil.execute(em -> {
                    TypedQuery<Guardian2Maps> query = em.createQuery("SELECT g2m FROM Guardian2Maps g2m " +
                            "WHERE g2m.bossGuardian IS NOT NULL AND g2m.guardian IS NULL AND g2m.scenario.id = :scenarioId", Guardian2Maps.class);
                    query.setParameter("scenarioId", game.getScenario().getId());

                    List<Guardian2Maps> bossGuardians = query.getResultList();
                    if (bossGuardians.isEmpty()) {
                        log.error("Boss guardians not found for scenarioId: " + game.getScenario().getId());
                        return;
                    }

                    bossGuardianArr[0] = bossGuardians.get(random.nextInt(bossGuardians.size()));

                    TypedQuery<Guardian2Maps> q = em.createQuery("SELECT g FROM Guardian2Maps g WHERE g.scenario.id = :scenarioId AND g.status.id = 1", Guardian2Maps.class);
                    q.setParameter("scenarioId", bossBattleScenario.getId());
                    List<Guardian2Maps> guardian2Maps = q.getResultList();

                    final List<Guardian2Maps> filteredList = new ArrayList<>();
                    filteredList.add(bossGuardianArr[0]);

                    for (Guardian2Maps g2m : guardian2Maps) {
                        if (filteredList.stream().anyMatch(f -> f.getGuardian() != null && f.getGuardian().getId().equals(g2m.getGuardian().getId()))) {
                            continue;
                        }
                        if (filteredList.stream().anyMatch(f -> f.getBossGuardian() != null && f.getBossGuardian().getId().equals(g2m.getBossGuardian().getId()))) {
                            continue;
                        }

                        filteredList.add(g2m);
                    }
                    game.getGuardiansInBossStage().clear();
                    game.getGuardiansInBossStage().addAll(filteredList);
                });
            }

            int activePlayingPlayersCount = game.getPlayerBattleStates().size();
            List<GuardianBase> guardians = game.determineGuardians(game.getGuardiansInBossStage(), game.getGuardianLevelLimit().get());
            game.getGuardianBattleStates().clear();

            BossGuardian bossGuardian = game.getGuardiansInBossStage().stream()
                    .filter(x -> x.getSide() == Guardian2Maps.Side.MIDDLE && x.getBossGuardian() != null)
                    .findFirst()
                    .map(Guardian2Maps::getBossGuardian)
                    .orElse(bossGuardianService.findBossGuardianById(1L));
            bossGuardian = bossGuardianService.findBossGuardianById(bossGuardian.getId());

            if (isAdvancedBossGuardianMode) {
                if (game.loadAdvancedBossGuardianMode("hb")) {
                    log.info("Advanced boss guardian mode loaded for map: " + game.getMap().getName() + ", scenarioId: " + game.getScenario().getId());
                } else {
                    log.info("Advanced boss guardian mode could not be loaded for map: " + game.getMap().getName() + ", scenarioId: " + game.getScenario().getId());
                }
            }

            if (!hasBossGuardianStage && isHardMode) {
                game.fillRemainingGuardianSlots(true, game, game.getGuardiansInBossStage(), guardians);
            }

            guardians.set(0, bossGuardian);

            GuardianBattleState bossBattleState = game.createGuardianBattleState(false, bossGuardian, (short) 10, activePlayingPlayersCount);
            game.getGuardianBattleStates().add(bossBattleState);

            byte guardianStartPosition = 10;

            for (int i = 1; i <  guardians.size(); i++) {
                GuardianBase guardianBase = guardians.get(i);
                if (guardianBase == null) continue;

                if (game.getIsRandomGuardiansMode().get()) {
                    guardianBase.setId(random.nextLong(72) + 1);
                }

                short guardianPosition = (short) (i + guardianStartPosition);
                if (guardianBase instanceof Guardian) {
                    guardianBase = guardianService.findGuardianById(guardianBase.getId());
                } else {
                    guardianBase = bossGuardianService.findBossGuardianById(guardianBase.getId());
                }
                if (game.getIsRandomGuardiansMode().get()) {
                    guardians.set(i, guardianBase);
                }

                GuardianBattleState guardianBattleState = game.createGuardianBattleState(game.getIsHardMode().get(), guardianBase, guardianPosition, activePlayingPlayersCount);
                game.getGuardianBattleStates().add(guardianBattleState);
            }

            S2CMatchplaySpawnBossBattle matchplaySpawnBossBattle = new S2CMatchplaySpawnBossBattle(guardians.get(0), guardians.get(1), guardians.get(2));
            S2CRoomSetBossGuardiansStats setBossGuardiansStats = new S2CRoomSetBossGuardiansStats(game.getGuardianBattleStates(), guardians);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(matchplaySpawnBossBattle, connection);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(setBossGuardiansStats, connection);

            RunnableEvent runnableEvent = eventHandler.createRunnableEvent(new GuardianServeTask(connection), TimeUnit.SECONDS.toMillis(18));
            gameSession.getFireables().push(runnableEvent);
            eventHandler.push(runnableEvent);

            final int activePlayerCount = game.getPlayerBattleStates().size();
            switch (activePlayerCount) {
                case 1, 2 -> {
                    runnableEvent = eventHandler.createRunnableEvent(new PlaceCrystalRandomlyTask(connection), TimeUnit.SECONDS.toMillis(18));
                    gameSession.getFireables().push(runnableEvent);
                    eventHandler.push(runnableEvent);
                }
                case 3, 4 -> {
                    runnableEvent = eventHandler.createRunnableEvent(new PlaceCrystalRandomlyTask(connection), TimeUnit.SECONDS.toMillis(18));
                    gameSession.getFireables().push(runnableEvent);
                    eventHandler.push(runnableEvent);

                    runnableEvent = eventHandler.createRunnableEvent(new PlaceCrystalRandomlyTask(connection), TimeUnit.SECONDS.toMillis(36));
                    gameSession.getFireables().push(runnableEvent);
                    eventHandler.push(runnableEvent);
                }
            }
        } else {
            stageChangingToBoss = game.getStageChangingToBoss().get();
            allGuardiansDead = game.getGuardianBattleStates().stream().allMatch(x -> x.getCurrentHealth().get() < 1);
            if (allGuardiansDead && !game.getFinished().get() && !stageChangingToBoss) {
                ThreadManager.getInstance().newTask(new FinishGameTask(connection));
            }
        }
    }

    private void handleAllPlayersDead(FTConnection connection, MatchplayGuardianGame game) {
        boolean allPlayersDead = game.getPlayerBattleStates().stream().allMatch(x -> x.getCurrentHealth().get() < 1);
        if (allPlayersDead && !game.getFinished().get()) {
            ThreadManager.getInstance().newTask(new FinishGameTask(connection));
        }
    }
}
