package com.jftse.emulator.server.core.handler.game.matchplay;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.manager.ThreadManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.battle.SkillUse;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.matchplay.C2SMatchplayUsesSkill;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayDealDamage;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayUseSkill;
import com.jftse.emulator.server.core.service.*;
import com.jftse.emulator.server.database.model.battle.Skill;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.QuickSlotEquipment;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.database.model.pocket.Pocket;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class PlayerUseSkillHandler extends AbstractHandler {
    private C2SMatchplayUsesSkill anyoneUsesSkill;

    private final SkillService skillService;
    private final AuthenticationService authenticationService;
    private final PlayerService playerService;
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;

    public PlayerUseSkillHandler() {
        this.skillService = ServiceManager.getInstance().getSkillService();
        this.authenticationService = ServiceManager.getInstance().getAuthenticationService();
        this.playerService = ServiceManager.getInstance().getPlayerService();
        this.pocketService = ServiceManager.getInstance().getPocketService();
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.quickSlotEquipmentService = ServiceManager.getInstance().getQuickSlotEquipmentService();
    }

    @Override
    public boolean process(Packet packet) {
        anyoneUsesSkill = new C2SMatchplayUsesSkill(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActiveGameSession() == null
                || connection.getClient().getActiveRoom() == null || connection.getClient().getActivePlayer() == null)
            return;

        byte attackerPosition = anyoneUsesSkill.getAttackerPosition();
        boolean attackerIsGuardian = attackerPosition > 9;
        boolean attackerIsPlayer = attackerPosition < 4;

        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGame game = gameSession.getActiveMatchplayGame();

        boolean isBattleGame = game instanceof MatchplayBattleGame;

        Room room = connection.getClient().getActiveRoom();

        RoomPlayer roomPlayer = room.getRoomPlayerList().stream()
                .filter(x -> x.getPlayer() != null && x.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                .findAny()
                .orElse(null);

        Skill skill = skillService.findSkillById((long) anyoneUsesSkill.getSkillIndex() + 1);
        if (skill == null)
            return;

        Instant now = Instant.now();
        SkillUse skillUse = new SkillUse(skill, anyoneUsesSkill, now.toEpochMilli(), false);
        boolean useSkill = false;

        if (attackerIsGuardian && !isBattleGame) {
            GuardianBattleState guardianBattleState = ((MatchplayGuardianGame) game).getGuardianBattleStates().stream()
                    .filter(x -> x.getPosition().get() == attackerPosition)
                    .findFirst()
                    .orElse(null);

            if (guardianBattleState != null) {
                int skillUseDequeSize = addSkillUseToSkillUseDeque(skillUse, guardianBattleState.getDex(), guardianBattleState.getSkillUseDeque());
                for (int i = 0; i < skillUseDequeSize; i++) {
                    try {
                        log.debug("PlayerUseSkillHandler: take() from deque\n" +
                                "skillUseDequeSize: " + skillUseDequeSize
                        );
                        SkillUse current = guardianBattleState.getSkillUseDeque().take();
                        log.debug("PlayerUseSkillHandler: taken skill\n" +
                                "skillUseDequeSize: " + skillUseDequeSize + "\n" +
                                "skill Name: " + current.getSkill().getName() + "\n" +
                                "anyoneUsesSkill.getAttackerPosition(): " + anyoneUsesSkill.getAttackerPosition() + "\n" +
                                "anyoneUsesSkill.getTargetPosition(): " + anyoneUsesSkill.getTargetPosition() + "\n" +
                                "current.getUsesSkill().getAttackerPosition(): " + current.getAttackerPosition() + "\n" +
                                "current.getUsesSkill().getTargetPosition(): " + current.getTargetPosition()
                        );

                        if (current.isSame(skillUse)) {
                            log.debug("is same skill");
                            if (current.isQuickSlot() == skillUse.isQuickSlot()) {
                                log.debug("is same skill by quickslot = true");

                                long coolingTime = skill.getGdCoolingTime().longValue();
                                if (skillUse.getTimestamp() != current.getTimestamp() && ((skillUse.getTimestamp() - current.getTimestamp()) < coolingTime)) {
                                    log.debug("skill cooling time didn't pass, don't add to queue");
                                } else {
                                    log.debug("current skill cooling time passed. put to queue");

                                    if (skillUse.isSpiderMine()) {
                                        handleSpiderMine(game, skillUse);
                                    }

                                    if (skillUse.isEarth()) {
                                        handleEarth(game, skillUse);
                                    }

                                    if (skillUse.isApollonFlash()) {
                                        handleApollonFlash(game, skillUse);
                                    }

                                    if (skillUse.isMiniam()) {
                                        handleMiniam(game, skillUse);
                                    }

                                    if (skillUse.isInferno()) {
                                        handleInferno(game, skillUse);
                                    }

                                    if (skillUse.isRangeShield()) {
                                        handleRangeShield(game, skillUse, guardianBattleState);
                                    }

                                    if (skillUse.isRangeHeal()) {
                                        handleRangeHeal(game, skillUse, guardianBattleState);
                                    }

                                    guardianBattleState.getSkillUseDeque().put(skillUse);
                                    log.debug("put to deque");
                                    useSkill = true;
                                    ScheduledFuture<?> scheduledFuture = ThreadManager.getInstance().schedule(() -> {
                                        if (skillUse != null) {
                                            final boolean playTimePassed = skillUse.getPlayTimePassed().get();
                                            if (!playTimePassed) {
                                                skillUse.getPlayTimePassed().set(true);
                                            }
                                        }
                                    }, skillUse.getSkill().getPlayTime().intValue(), TimeUnit.SECONDS);
                                    ((MatchplayGuardianGame) game).getScheduledFutures().offer(scheduledFuture);
                                    break;
                                }
                            } else {
                                if (skillUse.getTimestamp() == current.getTimestamp()) {
                                    log.debug("skill already added, don't add to queue");
                                } else {
                                    if (skillUse.isSpiderMine()) {
                                        handleSpiderMine(game, skillUse);
                                    }

                                    if (skillUse.isEarth()) {
                                        handleEarth(game, skillUse);
                                    }

                                    if (skillUse.isApollonFlash()) {
                                        handleApollonFlash(game, skillUse);
                                    }

                                    if (skillUse.isMiniam()) {
                                        handleMiniam(game, skillUse);
                                    }

                                    if (skillUse.isInferno()) {
                                        handleInferno(game, skillUse);
                                    }

                                    if (skillUse.isRangeShield()) {
                                        handleRangeShield(game, skillUse, guardianBattleState);
                                    }

                                    if (skillUse.isRangeHeal()) {
                                        handleRangeHeal(game, skillUse, guardianBattleState);
                                    }

                                    guardianBattleState.getSkillUseDeque().put(skillUse);
                                    useSkill = true;
                                    ScheduledFuture<?> scheduledFuture = ThreadManager.getInstance().schedule(() -> {
                                        if (skillUse != null) {
                                            final boolean playTimePassed = skillUse.getPlayTimePassed().get();
                                            if (!playTimePassed) {
                                                skillUse.getPlayTimePassed().set(true);
                                            }
                                        }
                                    }, skillUse.getSkill().getPlayTime().intValue(), TimeUnit.SECONDS);
                                    ((MatchplayGuardianGame) game).getScheduledFutures().offer(scheduledFuture);
                                    break;
                                }
                            }
                        } else {
                            log.debug("is not same skill. put to deque");
                            guardianBattleState.getSkillUseDeque().put(current);
                            log.debug("put to deque");
                        }
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
            if (useSkill) {
                this.handleSpecialSkillsUseOfGuardians(connection, attackerPosition, (MatchplayGuardianGame) game, room.getRoomPlayerList(), skill);
            }
        } else if (attackerIsPlayer) {
            if (roomPlayer != null) {
                PlayerBattleState playerBattleState = isBattleGame ?
                        ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                                .filter(x -> x.getPosition().get() == roomPlayer.getPosition())
                                .findFirst()
                                .orElse(null) :
                        ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                                .filter(x -> x.getPosition().get() == roomPlayer.getPosition())
                                .findFirst()
                                .orElse(null);

                if (playerBattleState != null) {
                    int skillUseDequeSize = addSkillUseToSkillUseDeque(skillUse, playerBattleState.getDex(), playerBattleState.getSkillUseDeque());
                    for (int i = 0; i < skillUseDequeSize; i++) {
                        try {
                            log.debug("PlayerUseSkillHandler: take() from deque\n" +
                                    "skillUseDequeSize: " + skillUseDequeSize
                            );
                            SkillUse current = playerBattleState.getSkillUseDeque().take();
                            log.debug("PlayerUseSkillHandler: taken skill\n" +
                                    "skillUseDequeSize: " + skillUseDequeSize + "\n" +
                                    "skill Name: " + current.getSkill().getName() + "\n" +
                                    "anyoneUsesSkill.getAttackerPosition(): " + anyoneUsesSkill.getAttackerPosition() + "\n" +
                                    "anyoneUsesSkill.getTargetPosition(): " + anyoneUsesSkill.getTargetPosition() + "\n" +
                                    "current.getUsesSkill().getAttackerPosition(): " + current.getAttackerPosition() + "\n" +
                                    "current.getUsesSkill().getTargetPosition(): " + current.getTargetPosition()
                            );

                            if (current.isSame(skillUse)) {
                                log.debug("is same skill");
                                if (current.isQuickSlot() == skillUse.isQuickSlot()) {
                                    log.debug("is same skill by quickslot = true");

                                    long coolingTime = isBattleGame ? skill.getCoolingTime().longValue() : skill.getGdCoolingTime().longValue();
                                    if (skillUse.getTimestamp() != current.getTimestamp() && ((skillUse.getTimestamp() - current.getTimestamp()) < coolingTime)) {
                                        log.debug("skill cooling time didn't pass, don't add to queue");
                                    } else {
                                        log.debug("current skill cooling time passed. put to queue");

                                        if (skillUse.isSpiderMine()) {
                                            handleSpiderMine(game, skillUse, playerBattleState);
                                        }

                                        if (skillUse.isEarth()) {
                                            handleEarth(game, skillUse, playerBattleState);
                                        }

                                        if (skillUse.isApollonFlash()) {
                                            handleApollonFlash(game, skillUse, playerBattleState);
                                        }

                                        if (skillUse.isMiniam()) {
                                            handleMiniam(game, skillUse, playerBattleState);
                                        }

                                        if (skillUse.isInferno()) {
                                            handleInferno(game, skillUse, playerBattleState);
                                        }

                                        if (skillUse.isRangeShield()) {
                                            handleRangeShield(game, skillUse, playerBattleState);
                                        }

                                        if (skillUse.isRangeHeal()) {
                                            handleRangeHeal(game, skillUse, playerBattleState);
                                        }

                                        playerBattleState.getSkillUseDeque().put(skillUse);
                                        log.debug("put to deque");
                                        useSkill = true;
                                        ScheduledFuture<?> scheduledFuture = ThreadManager.getInstance().schedule(() -> {
                                            if (skillUse != null) {
                                                final boolean playTimePassed = skillUse.getPlayTimePassed().get();
                                                if (!playTimePassed) {
                                                    skillUse.getPlayTimePassed().set(true);
                                                }
                                            }
                                        }, skillUse.getSkill().getPlayTime().intValue(), TimeUnit.SECONDS);
                                        if (isBattleGame)
                                            ((MatchplayBattleGame) game).getScheduledFutures().offer(scheduledFuture);
                                        else
                                            ((MatchplayGuardianGame) game).getScheduledFutures().offer(scheduledFuture);
                                        break;
                                    }
                                } else {
                                    if (skillUse.getTimestamp() == current.getTimestamp()) {
                                        log.debug("skill already added, don't add to queue");
                                    } else {
                                        if (skillUse.isSpiderMine()) {
                                            handleSpiderMine(game, skillUse, playerBattleState);
                                        }

                                        if (skillUse.isEarth()) {
                                            handleEarth(game, skillUse, playerBattleState);
                                        }

                                        if (skillUse.isApollonFlash()) {
                                            handleApollonFlash(game, skillUse, playerBattleState);
                                        }

                                        if (skillUse.isMiniam()) {
                                            handleMiniam(game, skillUse, playerBattleState);
                                        }

                                        if (skillUse.isInferno()) {
                                            handleInferno(game, skillUse, playerBattleState);
                                        }

                                        if (skillUse.isRangeShield()) {
                                            handleRangeShield(game, skillUse, playerBattleState);
                                        }

                                        if (skillUse.isRangeHeal()) {
                                            handleRangeHeal(game, skillUse, playerBattleState);
                                        }

                                        playerBattleState.getSkillUseDeque().put(skillUse);
                                        useSkill = true;
                                        ScheduledFuture<?> scheduledFuture = ThreadManager.getInstance().schedule(() -> {
                                            if (skillUse != null) {
                                                final boolean playTimePassed = skillUse.getPlayTimePassed().get();
                                                if (!playTimePassed) {
                                                    skillUse.getPlayTimePassed().set(true);
                                                }
                                            }
                                        }, skillUse.getSkill().getPlayTime().intValue(), TimeUnit.SECONDS);
                                        if (isBattleGame)
                                            ((MatchplayBattleGame) game).getScheduledFutures().offer(scheduledFuture);
                                        else
                                            ((MatchplayGuardianGame) game).getScheduledFutures().offer(scheduledFuture);
                                        break;
                                    }
                                }
                            } else {
                                log.debug("is not same skill. put to deque");
                                playerBattleState.getSkillUseDeque().put(current);
                                log.debug("put to deque");
                            }
                        } catch (InterruptedException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }

                if (useSkill) {
                    if (anyoneUsesSkill.isQuickSlot()) {
                        this.handleQuickSlotItemUse(connection, anyoneUsesSkill);
                    }
                }
            }
        }

        if (useSkill) {
            S2CMatchplayUseSkill packet =
                    new S2CMatchplayUseSkill(attackerPosition, anyoneUsesSkill.getTargetPosition(), anyoneUsesSkill.getSkillIndex(), anyoneUsesSkill.getSeed(), anyoneUsesSkill.getXTarget(), anyoneUsesSkill.getZTarget(), anyoneUsesSkill.getYTarget());
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(packet, connection);
        }
    }

    private int addSkillUseToSkillUseDeque(SkillUse skillUse, AtomicInteger dex, LinkedBlockingDeque<SkillUse> skillUseDeque) {
        skillUse.setShotCountByDex(dex.get());

        try {
            log.debug("PlayerUseSkillHandler: add skill. put to deque");
            skillUseDeque.put(skillUse);
            log.debug("put to deque");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        return skillUseDeque.size();
    }

    private ScheduledFuture<?> addSkillUseToSkillUseDeque(SkillUse skillUseForOtherPb, LinkedBlockingDeque<SkillUse> skillUseDeque) throws InterruptedException {
        skillUseDeque.put(skillUseForOtherPb);

        ScheduledFuture<?> scheduledFuture = ThreadManager.getInstance().schedule(() -> {
            if (skillUseForOtherPb != null) {
                final boolean playTimePassed = skillUseForOtherPb.getPlayTimePassed().get();
                if (!playTimePassed) {
                    skillUseForOtherPb.getPlayTimePassed().set(true);
                }
            }
        }, skillUseForOtherPb.getSkill().getPlayTime().intValue(), TimeUnit.SECONDS);
        return scheduledFuture;
    }

    private void internalAddSkillUse(MatchplayGame game, SkillUse skillUse, PlayerBattleState playerBattleState) throws InterruptedException {
        boolean isBattleGame = game instanceof MatchplayBattleGame;

        addSkillUseToPlayerOrGuardian(game, skillUse, playerBattleState, isBattleGame);
    }

    private void addSkillUseToPlayerOrGuardian(MatchplayGame game, SkillUse skillUse, PlayerBattleState playerBattleState, boolean isBattleGame) throws InterruptedException {
        if (isBattleGame) {
            boolean isRedTeam = game.isRedTeam(playerBattleState.getPosition().get());

            ConcurrentLinkedDeque<PlayerBattleState> playerBattleStates = new ConcurrentLinkedDeque<>(((MatchplayBattleGame) game).getPlayerBattleStates());
            int playerBattleStatesSize = playerBattleStates.size();
            for (int i = 0; i < playerBattleStatesSize; i++) {
                PlayerBattleState pb = playerBattleStates.poll();
                boolean isPbRedTeam = game.isRedTeam(pb.getPosition().get());
                if (pb.getPosition().get() != playerBattleState.getPosition().get() && ((isRedTeam && !isPbRedTeam) || (!isRedTeam && isPbRedTeam))) {
                    SkillUse skillUseForOtherPb = new SkillUse(skillUse.getSkill(), skillUse.getAttackerPosition(), (byte) pb.getPosition().get(), false, Instant.now().toEpochMilli(), false);
                    ScheduledFuture<?> scheduledFuture = this.addSkillUseToSkillUseDeque(skillUseForOtherPb, pb.getSkillUseDeque());
                    ((MatchplayBattleGame) game).getScheduledFutures().offer(scheduledFuture);
                }
            }
        } else {
            ConcurrentLinkedDeque<GuardianBattleState> guardianBattleStates = new ConcurrentLinkedDeque<>(((MatchplayGuardianGame) game).getGuardianBattleStates());
            int guardianBattleStatesSize = guardianBattleStates.size();
            for (int i = 0; i < guardianBattleStatesSize; i++) {
                GuardianBattleState gb = guardianBattleStates.poll();
                SkillUse skillUseForOtherPb = new SkillUse(skillUse.getSkill(), skillUse.getAttackerPosition(), (byte) gb.getPosition().get(), false, Instant.now().toEpochMilli(), false);
                ScheduledFuture<?> scheduledFuture = this.addSkillUseToSkillUseDeque(skillUseForOtherPb, gb.getSkillUseDeque());
                ((MatchplayGuardianGame) game).getScheduledFutures().offer(scheduledFuture);
            }
        }
    }

    private void addSkillUseToPlayer(MatchplayGuardianGame game, SkillUse skillUse) throws InterruptedException {
        ConcurrentLinkedDeque<PlayerBattleState> playerBattleStates = new ConcurrentLinkedDeque<>(game.getPlayerBattleStates());

        int playerBattleStatesSize = playerBattleStates.size();
        for (int i = 0; i < playerBattleStatesSize; i++) {
            PlayerBattleState pb = playerBattleStates.poll();
            SkillUse skillUseForOtherPb = new SkillUse(skillUse.getSkill(), skillUse.getAttackerPosition(), (byte) pb.getPosition().get(), false, Instant.now().toEpochMilli(), false);
            ScheduledFuture<?> scheduledFuture = this.addSkillUseToSkillUseDeque(skillUseForOtherPb, pb.getSkillUseDeque());
            game.getScheduledFutures().offer(scheduledFuture);
        }
    }

    private void handleSpiderMine(MatchplayGame game, SkillUse skillUse, PlayerBattleState playerBattleState) throws InterruptedException {
        boolean isBattleGame = game instanceof MatchplayBattleGame;

        synchronized (game) {
            final int spiderMineEffectId = isBattleGame ?
                    ((MatchplayBattleGame) game).getSpiderMineIdentifier().getAndIncrement() :
                    ((MatchplayGuardianGame) game).getSpiderMineIdentifier().getAndIncrement();
            skillUse.setSpiderMineEffectId(spiderMineEffectId);

            final int spiderMineId = isBattleGame ?
                    ((MatchplayBattleGame) game).getSpiderMineIdentifier().get() :
                    ((MatchplayGuardianGame) game).getSpiderMineIdentifier().get();
            skillUse.setSpiderMineId(spiderMineId);
        }

        addSkillUseToPlayerOrGuardian(game, skillUse, playerBattleState, isBattleGame);
    }

    private void handleSpiderMine(MatchplayGame game, SkillUse skillUse) throws InterruptedException {
        synchronized (game) {
            final int spiderMineEffectId = ((MatchplayGuardianGame) game).getSpiderMineIdentifier().getAndIncrement();
            skillUse.setSpiderMineEffectId(spiderMineEffectId);

            final int spiderMineId = ((MatchplayGuardianGame) game).getSpiderMineIdentifier().get();
            skillUse.setSpiderMineId(spiderMineId);
        }

        addSkillUseToPlayer((MatchplayGuardianGame) game, skillUse);
    }

    private void handleEarth(MatchplayGame game, SkillUse skillUse, PlayerBattleState playerBattleState) throws InterruptedException {
        internalAddSkillUse(game, skillUse, playerBattleState);
    }

    private void handleEarth(MatchplayGame game, SkillUse skillUse) throws InterruptedException {
        addSkillUseToPlayer((MatchplayGuardianGame) game, skillUse);
    }

    private void handleApollonFlash(MatchplayGame game, SkillUse skillUse, PlayerBattleState playerBattleState) throws InterruptedException {
        internalAddSkillUse(game, skillUse, playerBattleState);
    }

    private void handleApollonFlash(MatchplayGame game, SkillUse skillUse) throws InterruptedException {
        addSkillUseToPlayer((MatchplayGuardianGame) game, skillUse);
    }

    private void handleMiniam(MatchplayGame game, SkillUse skillUse, PlayerBattleState playerBattleState) throws InterruptedException {
        internalAddSkillUse(game, skillUse, playerBattleState);
    }

    private void handleMiniam(MatchplayGame game, SkillUse skillUse) throws InterruptedException {
        addSkillUseToPlayer((MatchplayGuardianGame) game, skillUse);
    }

    private void handleInferno(MatchplayGame game, SkillUse skillUse, PlayerBattleState playerBattleState) throws InterruptedException {
        internalAddSkillUse(game, skillUse, playerBattleState);
    }

    private void handleInferno(MatchplayGame game, SkillUse skillUse) throws InterruptedException {
        addSkillUseToPlayer((MatchplayGuardianGame) game, skillUse);
    }

    private void handleRangeShield(MatchplayGame game, SkillUse skillUse, PlayerBattleState playerBattleState) throws InterruptedException {
        boolean isBattleGame = game instanceof MatchplayBattleGame;

        ConcurrentLinkedDeque<PlayerBattleState> playerBattleStates = new ConcurrentLinkedDeque<>(isBattleGame ?
                ((MatchplayBattleGame) game).getPlayerBattleStates() :
                ((MatchplayGuardianGame) game).getPlayerBattleStates());

        playerBattleStates.removeIf(pb -> isBattleGame &&
                ((game.isRedTeam(playerBattleState.getPosition().get()) && !game.isRedTeam(pb.getPosition().get())) ||
                        (!game.isRedTeam(playerBattleState.getPosition().get()) && game.isRedTeam(pb.getPosition().get()))));

        int playerBattleStatesSize = playerBattleStates.size();
        for (int i = 0; i < playerBattleStatesSize; i++) {
            PlayerBattleState pb = playerBattleStates.poll();
            if (pb.getPosition().get() != playerBattleState.getPosition().get()) {
                SkillUse skillUseForOtherPb = new SkillUse(skillUse.getSkill(), (byte) 4, (byte) pb.getPosition().get(), false, Instant.now().toEpochMilli(), false);
                ScheduledFuture<?> scheduledFuture = this.addSkillUseToSkillUseDeque(skillUseForOtherPb, pb.getSkillUseDeque());
                if (isBattleGame)
                    ((MatchplayBattleGame) game).getScheduledFutures().offer(scheduledFuture);
                else
                    ((MatchplayGuardianGame) game).getScheduledFutures().offer(scheduledFuture);
            }
        }
    }

    private void handleRangeShield(MatchplayGame game, SkillUse skillUse, GuardianBattleState guardianBattleState) throws InterruptedException {
        ConcurrentLinkedDeque<GuardianBattleState> guardianBattleStates = new ConcurrentLinkedDeque<>(((MatchplayGuardianGame) game).getGuardianBattleStates());

        int guardianBattleStatesSize = guardianBattleStates.size();
        for (int i = 0; i < guardianBattleStatesSize; i++) {
            GuardianBattleState gb = guardianBattleStates.poll();
            if (gb.getPosition().get() != guardianBattleState.getPosition().get()) {
                SkillUse skillUseForOtherPb = new SkillUse(skillUse.getSkill(), (byte) 4, (byte) gb.getPosition().get(), false, Instant.now().toEpochMilli(), false);
                ScheduledFuture<?> scheduledFuture = this.addSkillUseToSkillUseDeque(skillUseForOtherPb, gb.getSkillUseDeque());
                ((MatchplayGuardianGame) game).getScheduledFutures().offer(scheduledFuture);
            }
        }
    }

    private void handleRangeHeal(MatchplayGame game, SkillUse skillUse, PlayerBattleState playerBattleState) throws InterruptedException {
        boolean isBattleGame = game instanceof MatchplayBattleGame;

        ConcurrentLinkedDeque<PlayerBattleState> playerBattleStates = new ConcurrentLinkedDeque<>(isBattleGame ?
                ((MatchplayBattleGame) game).getPlayerBattleStates() :
                ((MatchplayGuardianGame) game).getPlayerBattleStates());

        playerBattleStates.removeIf(pb -> isBattleGame &&
                ((game.isRedTeam(playerBattleState.getPosition().get()) && !game.isRedTeam(pb.getPosition().get())) ||
                        (!game.isRedTeam(playerBattleState.getPosition().get()) && game.isRedTeam(pb.getPosition().get()))));

        int playerBattleStatesSize = playerBattleStates.size();
        for (int i = 0; i < playerBattleStatesSize; i++) {
            PlayerBattleState pb = playerBattleStates.poll();
            if (pb.getPosition().get() != playerBattleState.getPosition().get()) {
                SkillUse skillUseForOtherPb = new SkillUse(skillUse.getSkill(), (byte) 4, (byte) pb.getPosition().get(), false, Instant.now().toEpochMilli(), false);
                skillUseForOtherPb.getSkill().setPlayTime(3.0);
                ScheduledFuture<?> scheduledFuture = this.addSkillUseToSkillUseDeque(skillUseForOtherPb, pb.getSkillUseDeque());
                if (isBattleGame)
                    ((MatchplayBattleGame) game).getScheduledFutures().offer(scheduledFuture);
                else
                    ((MatchplayGuardianGame) game).getScheduledFutures().offer(scheduledFuture);
            }
        }
    }

    private void handleRangeHeal(MatchplayGame game, SkillUse skillUse, GuardianBattleState guardianBattleState) throws InterruptedException {
        ConcurrentLinkedDeque<GuardianBattleState> guardianBattleStates = new ConcurrentLinkedDeque<>(((MatchplayGuardianGame) game).getGuardianBattleStates());

        int guardianBattleStatesSize = guardianBattleStates.size();
        for (int i = 0; i < guardianBattleStatesSize; i++) {
            GuardianBattleState gb = guardianBattleStates.poll();
            if (gb.getPosition().get() != guardianBattleState.getPosition().get()) {
                SkillUse skillUseForOtherPb = new SkillUse(skillUse.getSkill(), (byte) 4, (byte) gb.getPosition().get(), false, Instant.now().toEpochMilli(), false);
                skillUseForOtherPb.getSkill().setPlayTime(3.0);
                ScheduledFuture<?> scheduledFuture = this.addSkillUseToSkillUseDeque(skillUseForOtherPb, gb.getSkillUseDeque());
                ((MatchplayGuardianGame) game).getScheduledFutures().offer(scheduledFuture);
            }
        }
    }

    private void handleQuickSlotItemUse(Connection connection, C2SMatchplayUsesSkill playerUseSkill) {
        Player player = playerService.findById(connection.getClient().getActivePlayer().getId());
        Pocket pocket = player.getPocket();

        QuickSlotEquipment quickSlotEquipment = player.getQuickSlotEquipment();
        int itemId = -1;
        switch (playerUseSkill.getQuickSlotIndex()) {
            case 0:
                itemId = quickSlotEquipment.getSlot1();
                break;
            case 1:
                itemId = quickSlotEquipment.getSlot2();
                break;
            case 2:
                itemId = quickSlotEquipment.getSlot3();
                break;
            case 3:
                itemId = quickSlotEquipment.getSlot4();
                break;
            case 4:
                itemId = quickSlotEquipment.getSlot5();
                break;
        }

        if (itemId > -1) {
            PlayerPocket playerPocket = playerPocketService.getItemAsPocket((long) itemId, pocket);
            if (playerPocket != null) {
                int itemCount = playerPocket.getItemCount() - 1;

                if (itemCount <= 0) {

                    playerPocketService.remove(playerPocket.getId());
                    pocket = pocketService.decrementPocketBelongings(pocket);
                    connection.getClient().getActivePlayer().setPocket(pocket);

                    quickSlotEquipmentService.updateQuickSlots(quickSlotEquipment, itemId);
                    player.setQuickSlotEquipment(quickSlotEquipment);

                    player = playerService.save(player);
                    connection.getClient().setActivePlayer(player);

                    S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(itemId);
                    connection.sendTCP(inventoryItemRemoveAnswerPacket);
                } else {
                    playerPocket.setItemCount(itemCount);
                    playerPocketService.save(playerPocket);
                }
            }
        }
    }

    private void handleSpecialSkillsUseOfGuardians(Connection connection, byte guardianPos, MatchplayGuardianGame game, ConcurrentLinkedDeque<RoomPlayer> roomPlayers, Skill skill) {
        // There could be more special skills which need to be handled here
        if (skill.getId() == 29) { // RebirthOne
            this.handleReviveGuardian(connection, game, skill);
        } else if (skill.getDamage() > 1) {
            Short newHealth;
            try {
                newHealth = game.getGuardianCombatSystem().heal(guardianPos, skill.getDamage().shortValue());
            } catch (ValidationException ve) {
                log.warn(ve.getMessage());
                return;
            }

            S2CMatchplayDealDamage damagePacket =
                    new S2CMatchplayDealDamage(guardianPos, newHealth, skill.getTargeting().shortValue(), skill.getId().byteValue(), 0, 0);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(damagePacket, connection);
        }
    }

    private void handleReviveGuardian(Connection connection, MatchplayGuardianGame game, Skill skill) {
        GuardianBattleState guardianBattleState = null;
        try {
            guardianBattleState = game.getGuardianCombatSystem().reviveAnyGuardian(skill.getDamage().shortValue());
        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
        }

        if (guardianBattleState != null) {
            S2CMatchplayDealDamage damageToPlayerPacket =
                    new S2CMatchplayDealDamage((short) guardianBattleState.getPosition().get(), (short) guardianBattleState.getCurrentHealth().get(), (short) 0, skill.getId().byteValue(), 0, 0);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(damageToPlayerPacket, connection);
        }
    }
}
