package com.jftse.emulator.server.game.core.game.handler.matchplay;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.database.model.battle.*;
import com.jftse.emulator.server.database.model.item.Product;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.QuickSlotEquipment;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.database.model.pocket.Pocket;
import com.jftse.emulator.server.game.core.constants.GameFieldSide;
import com.jftse.emulator.server.game.core.constants.PacketEventType;
import com.jftse.emulator.server.game.core.constants.RoomStatus;
import com.jftse.emulator.server.game.core.item.EItemCategory;
import com.jftse.emulator.server.game.core.item.EItemUseType;
import com.jftse.emulator.server.game.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.game.core.matchplay.PlayerReward;
import com.jftse.emulator.server.game.core.matchplay.basic.MatchplayGuardianGame;
import com.jftse.emulator.server.game.core.matchplay.battle.*;
import com.jftse.emulator.server.game.core.matchplay.event.PacketEventHandler;
import com.jftse.emulator.server.game.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.game.core.matchplay.event.RunnableEventHandler;
import com.jftse.emulator.server.game.core.matchplay.room.GameSession;
import com.jftse.emulator.server.game.core.matchplay.room.Room;
import com.jftse.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.game.core.packet.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.game.core.packet.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.emulator.server.game.core.packet.packets.lobby.room.S2CRoomMapChangeAnswerPacket;
import com.jftse.emulator.server.game.core.packet.packets.lobby.room.S2CRoomSetBossGuardiansStats;
import com.jftse.emulator.server.game.core.packet.packets.lobby.room.S2CRoomSetGuardianStats;
import com.jftse.emulator.server.game.core.packet.packets.lobby.room.S2CRoomSetGuardians;
import com.jftse.emulator.server.game.core.packet.packets.matchplay.*;
import com.jftse.emulator.server.game.core.service.*;
import com.jftse.emulator.server.game.core.utils.BattleUtils;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;
import com.jftse.emulator.server.shared.module.GameHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class GuardianModeHandler {
    private final static long guardianAttackLoopTime = TimeUnit.SECONDS.toMillis(8);

    private final PacketEventHandler packetEventHandler;
    private final RunnableEventHandler runnableEventHandler;
    private final SkillService skillService;
    private final GuardianService guardianService;
    private final BossGuardianService bossGuardianService;
    private final GuardianStageService guardianStageService;
    private final PlayerPocketService playerPocketService;
    private final PlayerService playerService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;
    private final PocketService pocketService;
    private final LevelService levelService;
    private final ClothEquipmentService clothEquipmentService;
    private final GameSessionManager gameSessionManager;
    private final WillDamageService willDamageService;
    private final GuardianSkillsService guardianSkillsService;
    private final ProductService productService;

    private GameHandler gameHandler;

    private Random random;

    public void init(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.random = new Random();
    }

    public void handleGuardianModeMatchplayPointPacket(Connection connection, C2SMatchplayPointPacket matchplayPointPacket, GameSession gameSession, MatchplayGuardianGame game) {
        boolean lastGuardianServeWasOnGuardianSide = game.getLastGuardianServeSide() == GameFieldSide.Guardian;
        int servingPositionXOffset = random.nextInt(7);
        if (!lastGuardianServeWasOnGuardianSide) {
            game.setLastGuardianServeSide(GameFieldSide.Guardian);
            S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, (byte) servingPositionXOffset, (byte) 0);
            gameSession.getClients().forEach(x -> x.getConnection().sendTCP(triggerGuardianServePacket));
        } else {
            game.setLastGuardianServeSide(GameFieldSide.Players);
            S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Players, (byte) servingPositionXOffset, (byte) 0);
            gameSession.getClients().forEach(x -> x.getConnection().sendTCP(triggerGuardianServePacket));
        }
    }

    public void handleStartGuardianMode(Connection connection, Room room) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();
        game.setLastGuardianServeSide(GameFieldSide.Guardian);
        List<Client> clients = this.gameHandler.getClientsInRoom(room.getRoomId());

        int servingPositionXOffset = random.nextInt(7);
        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, (byte) servingPositionXOffset, (byte) 0);
        clients.forEach(c -> {
            c.getConnection().sendTCP(triggerGuardianServePacket);
        });

        game.resetStageStartTime();

        int activePlayers = (int) game.getPlayerBattleStates().stream().count();
        switch (activePlayers) {
            case 1:
            case 2:
                game.setCrystalSpawnInterval(TimeUnit.SECONDS.toMillis(5));
                game.setCrystalDeSpawnInterval(TimeUnit.SECONDS.toMillis(8));
                this.placeCrystalRandomly(connection, game);
                break;
            case 3:
            case 4:
                game.setCrystalSpawnInterval(TimeUnit.SECONDS.toMillis(5));
                game.setCrystalDeSpawnInterval(TimeUnit.SECONDS.toMillis(7));
                this.placeCrystalRandomly(connection, game);
                this.placeCrystalRandomly(connection, game);
                break;
        }

        this.triggerGuardianAttackLoop(connection);
        this.startDefeatTimer(connection, game, gameSession, game.getGuardianStage());
        gameSession.setSpeedHackCheckActive(true);
    }

    public void handlePrepareGuardianMode(Connection connection, Room room) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();
        game.setWillDamages(this.willDamageService.getWillDamages());

        List<RoomPlayer> roomPlayers = room.getRoomPlayerList();

        float averagePlayerLevel = this.getAveragePlayerLevel(roomPlayers);
        this.handleMonsLavaMap(connection, room, averagePlayerLevel);

        GuardianStage guardianStage = this.guardianStageService.getGuardianStages().stream()
                .filter(x -> x.getMapId() == room.getMap() && !x.getIsBossStage())
                .findFirst()
                .orElse(null);
        game.setGuardianStage(guardianStage);
        game.setCurrentStage(guardianStage);

        GuardianStage bossGuardianStage = this.guardianStageService.getGuardianStages().stream()
                .filter(x -> x.getMapId() == room.getMap() && x.getIsBossStage())
                .findFirst()
                .orElse(null);
        game.setBossGuardianStage(bossGuardianStage);

        int guardianLevelLimit = this.getGuardianLevelLimit(averagePlayerLevel);
        game.setGuardianLevelLimit(guardianLevelLimit);

        List<PlayerBattleState> playerBattleStates = roomPlayers.stream()
                .filter(x -> x.getPosition() < 4)
                .map(rp -> this.createPlayerBattleState(rp))
                .collect(Collectors.toList());
        game.setPlayerBattleStates(playerBattleStates);

        int activePlayingPlayersCount = (int) roomPlayers.stream().filter(x -> x.getPosition() < 4).count();
        byte guardianStartPosition = 10;
        List<Byte> guardians = this.determineGuardians(game.getGuardianStage(), game.getGuardianLevelLimit());
        for (int i = 0; i < guardians.stream().count(); i++) {
            int guardianId = guardians.get(i);
            if (guardianId == 0) continue;

            short guardianPosition = (short) (i + guardianStartPosition);
            Guardian guardian = guardianService.findGuardianById((long) guardianId);
            GuardianBattleState guardianBattleState = this.createGuardianBattleState(guardian, guardianPosition, activePlayingPlayersCount);
            game.getGuardianBattleStates().add(guardianBattleState);
        }

        S2CRoomSetGuardians roomSetGuardians = new S2CRoomSetGuardians(guardians.get(0), guardians.get(1), guardians.get(2));
        S2CRoomSetGuardianStats roomSetGuardianStats = new S2CRoomSetGuardianStats(game.getGuardianBattleStates());
        List<Client> clients = this.gameHandler.getClientsInRoom(room.getRoomId());
        clients.forEach(c -> {
            c.getConnection().sendTCP(roomSetGuardians);
            c.getConnection().sendTCP(roomSetGuardianStats);
        });
    }

    public void handlePlayerPickingUpCrystal(Connection connection, C2SMatchplayPlayerPicksUpCrystal playerPicksUpCrystalPacket) {
        // sometimes we are faster when cleaning up game sessions till the player is thrown back to the room
        if (connection.getClient().getActiveGameSession() == null) return;
        
        RoomPlayer roomPlayer = this.getRoomPlayerFromConnection(connection);
        short playerPosition = roomPlayer.getPosition();
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();
        SkillCrystal skillCrystal = game.getSkillCrystals().stream()
                .filter(x -> x.getId() == playerPicksUpCrystalPacket.getCrystalId())
                .findFirst()
                .orElse(null);

        if (skillCrystal != null) {
            if (gameSession == null) return;
            S2CMatchplayGiveRandomSkill randomGuardianSkill =
                    new S2CMatchplayGiveRandomSkill(playerPicksUpCrystalPacket.getCrystalId(), (byte) playerPosition);
            this.sendPacketToAllClientsInSameGameSession(randomGuardianSkill, connection);

            game.getSkillCrystals().remove(skillCrystal);
            RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(() -> this.placeCrystalRandomly(connection, game), game.getCrystalSpawnInterval());
            gameSession.getRunnableEvents().add(runnableEvent);
        }
    }

    public void handleUseOfSkill(Connection connection, C2SMatchplayUsesSkill anyoneUsesSkill) {
        byte position = anyoneUsesSkill.getAttackerPosition();
        boolean attackerIsGuardian = position > 9;
        boolean attackerIsPlayer = position < 4;
        GameSession gameSession = connection.getClient().getActiveGameSession();
        // sometimes we are faster when cleaning up game sessions till the player is thrown back to the room
        if (gameSession == null) return;

        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();
        if (game == null) return;

        Room room = connection.getClient().getActiveRoom();
        List<RoomPlayer> roomPlayers = room.getRoomPlayerList();

        if (attackerIsGuardian) {
            Skill skill = skillService.findSkillById((long)anyoneUsesSkill.getSkillIndex() + 1);
            if (skill != null) {
                this.handleSpecialSkillsUseOfGuardians(connection, position, game, roomPlayers, skill);
            }
        } else if (attackerIsPlayer) {
            if (anyoneUsesSkill.isQuickSlot()) {
                this.handleQuickSlotItemUse(connection, anyoneUsesSkill);
            }
        }

        S2CMatchplayUseSkill packet =
                new S2CMatchplayUseSkill(position, anyoneUsesSkill.getTargetPosition(), anyoneUsesSkill.getSkillIndex(), anyoneUsesSkill.getSeed(), anyoneUsesSkill.getXTarget(), anyoneUsesSkill.getZTarget(), anyoneUsesSkill.getYTarget());
        gameSession.getClients().forEach(c -> {
            if (c.getConnection().getId() != connection.getId()) {
                c.getConnection().sendTCP(packet);
            }
        });
    }

    public void handleSkillHitsTarget(Connection connection, C2SMatchplaySkillHitsTarget skillHitsTarget) {
        // sometimes we are faster when cleaning up game sessions till the player is thrown back to the room
        if (connection.getClient().getActiveGameSession() == null) return;

        byte skillId = skillHitsTarget.getSkillId();
        GameSession gameSession = connection.getClient().getActiveGameSession();

        // Until speed hack detection is not active do nothing here. This means we are in animations and the actual game is currently not started yet
        if (!gameSession.isSpeedHackCheckActive()) return;

        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();
        Skill skill = skillService.findSkillById((long)skillId);

        if (skill != null && this.isUniqueSkill(skill)) {
            this.handleUniqueSkill(connection, game, skill, skillHitsTarget);
            return;
        }

        boolean denyDamage = skillHitsTarget.getDamageType() == 1;
        if (skillId == 0 && !denyDamage) {
            this.handleBallLossDamage(connection, skillHitsTarget);
        } else {
            this.handleSkillDamage(connection, skillHitsTarget.getTargetPosition(), skillHitsTarget, game, skill);
        }

        this.handleAllGuardiansDead(connection, game);
        this.handleAllPlayersDead(connection, game);
    }

    public void handleSwapQuickSlotItems(Connection connection, C2SMatchplaySwapQuickSlotItems swapQuickSlotItems) {
        // sometimes we are faster when cleaning up game sessions till the player is thrown back to the room
        if (connection.getClient().getActiveGameSession() == null) return;

        RoomPlayer roomPlayer = this.getRoomPlayerFromConnection(connection);
        Pocket pocket = roomPlayer.getPlayer().getPocket();
        PlayerPocket playerPocket = this.playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(21, EItemCategory.SPECIAL.getName(), pocket);
        if (playerPocket != null) {
            playerPocket = this.playerPocketService.decrementPocketItemCount(playerPocket);
            if (playerPocket.getItemCount() == 0) {
                playerPocketService.remove(playerPocket.getId());
                pocketService.decrementPocketBelongings(pocket);
            }
        }

        S2CMatchplayGivePlayerSkills givePlayerSkills
                = new S2CMatchplayGivePlayerSkills(roomPlayer.getPosition(), swapQuickSlotItems.getTargetLeftSlotSkill(), swapQuickSlotItems.getTargetRightSlotSkill());
        this.sendPacketToAllClientsInSameGameSession(givePlayerSkills, connection);
    }

    private void triggerGuardianAttackLoop(Connection connection) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession == null) return;
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();

        RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(() -> {
            game.getGuardianBattleStates().forEach(x -> {
                int skillIndex = this.getRandomGuardianSkillBasedOnProbability(x.getBtItemId());
                S2CMatchplayGiveSpecificSkill packet = new S2CMatchplayGiveSpecificSkill((short) 0, x.getPosition(), skillIndex);
                this.sendPacketToAllClientsInSameGameSession(packet, connection);
            });

            this.triggerGuardianAttackLoop(connection);
        }, this.guardianAttackLoopTime);
        gameSession.getRunnableEvents().add(runnableEvent);
    }

    private void handleBallLossDamage(Connection connection, C2SMatchplaySkillHitsTarget skillHitsTarget) {
        short receiverPosition = skillHitsTarget.getTargetPosition();
        short attackerPosition = skillHitsTarget.getAttackerPosition();
        boolean attackerHasWillBuff = skillHitsTarget.getAttackerBuffId() == 3;

        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();

        boolean guardianMadePoint = skillHitsTarget.getTargetPosition() < 4;
        short newHealth;
        try {
            if (guardianMadePoint) {
                if (!skillHitsTarget.isApplySkillEffect()) {
                    return;
                }

                newHealth = game.damagePlayerOnBallLoss(receiverPosition, attackerPosition, attackerHasWillBuff);
            } else {
                newHealth = game.damageGuardianOnBallLoss(receiverPosition, attackerPosition, attackerHasWillBuff);
                if (newHealth < 1) {
                    this.increasePotsFromGuardiansDeath(game, receiverPosition);
                }
            }
        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
            return;
        }

        S2CMatchplayDealDamage damageToGuardianPacket = new S2CMatchplayDealDamage(skillHitsTarget.getTargetPosition(), newHealth, (short) 0, (byte) 0, 0, 0);
        this.sendPacketToAllClientsInSameGameSession(damageToGuardianPacket, connection);
    }

    private void handleSkillDamage(Connection connection, short targetPosition, C2SMatchplaySkillHitsTarget skillHitsTarget, MatchplayGuardianGame game, Skill skill) {
        boolean denyDamage = skillHitsTarget.getDamageType() == 1;
        short attackerPosition = skillHitsTarget.getAttackerPosition();
        boolean attackerHasStrBuff = skillHitsTarget.getAttackerPosition() == 0;
        boolean receiverHasDefBuff = skillHitsTarget.getReceiverBuffId() == 1;

        short skillDamage = skill != null ? skill.getDamage().shortValue() : -1;
        short newHealth;
        if (targetPosition < 4) {
            try {
                if (skillDamage > 1) {
                    newHealth = game.healPlayer(targetPosition, skillDamage);
                } else if (denyDamage) {
                    newHealth = game.damagePlayer(attackerPosition, targetPosition, (short) -1, false, false);
                } else if (skillDamage == 0) {
                    newHealth = game.getPlayerCurrentHealth(targetPosition);
                } else if (!skillHitsTarget.isApplySkillEffect()) {
                    return;
                } else {
                    newHealth = game.damagePlayer(attackerPosition, targetPosition, skillDamage, attackerHasStrBuff, receiverHasDefBuff);
                }
            } catch (ValidationException ve) {
                log.warn(ve.getMessage());
                return;
            }
        } else {
            try {
                if (skillDamage > 1) {
                    newHealth = game.healGuardian(targetPosition, skillDamage);
                } else if (denyDamage) {
                    newHealth = game.damageGuardian(attackerPosition, targetPosition, (short) -1, false, false);
                } else {
                    newHealth = game.damageGuardian(targetPosition, attackerPosition, skillDamage, attackerHasStrBuff, receiverHasDefBuff);
                }
            } catch (ValidationException ve) {
                log.warn(ve.getMessage());
                return;
            }

            if (newHealth < 1) {
                this.increasePotsFromGuardiansDeath(game, targetPosition);
            }
        }

        byte skillToApply = this.getSkillToApply(skill, skillHitsTarget);
        S2CMatchplayDealDamage damageToPlayerPacket =
                new S2CMatchplayDealDamage(targetPosition, newHealth, (short) 0, skillToApply, skillHitsTarget.getXKnockbackPosition(), skillHitsTarget.getYKnockbackPosition());
        this.sendPacketToAllClientsInSameGameSession(damageToPlayerPacket, connection);
    }

    private byte getSkillToApply(Skill skill, C2SMatchplaySkillHitsTarget skillHitsTarget) {
        boolean targetHittingHimself = skillHitsTarget.getAttackerPosition() == skillHitsTarget.getTargetPosition();
        if (skill != null && skill.getId() == 64 && targetHittingHimself) {
            return 3;
        }

        if (!skillHitsTarget.isApplySkillEffect()) {
            return 3;
        }

        return skillHitsTarget.getSkillId();
    }

    private void increasePotsFromGuardiansDeath(MatchplayGuardianGame game, int guardianPos) {
        GuardianBattleState guardianBattleState = game.getGuardianBattleStates().stream()
                .filter(x -> x.getPosition() == guardianPos)
                .findFirst()
                .orElse(null);
        if (guardianBattleState != null && !guardianBattleState.isLooted()) {
            guardianBattleState.setLooted(true);
            game.setExpPot(game.getExpPot() + guardianBattleState.getExp());
            game.setGoldPot(game.getGoldPot() + guardianBattleState.getGold());
        }
    }

    private boolean isUniqueSkill(Skill skill) {
        int skillId = skill.getId().intValue();
        return skillId == 5 || skillId == 38;
    }

    private void handleSpecialSkillsUseOfGuardians(Connection connection, byte guardianPos, MatchplayGuardianGame game, List<RoomPlayer> roomPlayers, Skill skill) {
        // There could be more special skills which need to be handled here
        if (skill.getId() == 29) { // RebirthOne
            this.handleReviveGuardian(connection, game, skill);
        } else if (skill.getDamage() > 1) {
            Short newHealth;
            try {
                newHealth = game.healGuardian(guardianPos, skill.getDamage().shortValue());
            } catch (ValidationException ve) {
                log.warn(ve.getMessage());
                return;
            }

            S2CMatchplayDealDamage damagePacket =
                    new S2CMatchplayDealDamage(guardianPos, newHealth, skill.getTargeting().shortValue(), skill.getId().byteValue(), 0, 0);
            this.sendPacketToAllClientsInSameGameSession(damagePacket, connection);
        } else if (skill.getId() == 9) { // Miniam needs to be treated individually
            roomPlayers.forEach(rp -> {
                Short newHealth;
                try {
                    newHealth = game.damagePlayer(guardianPos, rp.getPosition(), skill.getDamage().shortValue(), false, false);
                } catch (ValidationException ve) {
                    log.warn(ve.getMessage());
                    return;
                }

                S2CMatchplayDealDamage damagePacket =
                        new S2CMatchplayDealDamage(rp.getPosition(), newHealth, skill.getTargeting().shortValue(), skill.getId().byteValue(), 0, 0);
                this.sendPacketToAllClientsInSameGameSession(damagePacket, connection);
            });
        }
    }

    private void handleUniqueSkill(Connection connection, MatchplayGuardianGame game, Skill skill, C2SMatchplaySkillHitsTarget skillHitsTarget) {
        int skillId = skill.getId().intValue();
        switch (skillId) {
            case 5: // Revive
                this.handleRevivePlayer(connection, game, skill, skillHitsTarget);
                break;
            case 38: // Sandglass
                GameSession gameSession = connection.getClient().getActiveGameSession();
                if (gameSession != null) {
                    RunnableEvent countDownRunnable = gameSession.getCountDownRunnable();
                    if (countDownRunnable != null) {
                        countDownRunnable.setEventFireTime(countDownRunnable.getEventFireTime() + TimeUnit.SECONDS.toMillis(60));
                        gameSession.getClients().forEach(c -> {
                            c.getConnection().sendTCP(new S2CMatchplayIncreaseBreathTimerBy60Seconds());
                        });
                    }
                }
                break;
        }
    }

    private PlayerBattleState createPlayerBattleState(RoomPlayer roomPlayer) {
        short baseHp = (short) BattleUtils.calculatePlayerHp(roomPlayer.getPlayer());
        short baseStr = roomPlayer.getPlayer().getStrength();
        short baseSta = roomPlayer.getPlayer().getStamina();
        short baseDex = roomPlayer.getPlayer().getDexterity();
        short baseWill = roomPlayer.getPlayer().getWillpower();
        short totalHp = (short) (baseHp + roomPlayer.getStatusPointsAddedDto().getAddHp());
        short totalStr = (short) (baseStr + roomPlayer.getStatusPointsAddedDto().getStrength());
        short totalSta = (short) (baseSta + roomPlayer.getStatusPointsAddedDto().getStamina());
        short totalDex = (short) (baseDex + roomPlayer.getStatusPointsAddedDto().getDexterity());
        short totalWill = (short) (baseWill + roomPlayer.getStatusPointsAddedDto().getWillpower());
        return new PlayerBattleState(roomPlayer.getPosition(), totalHp, totalStr, totalSta, totalDex, totalWill);
    }

    private GuardianBattleState createGuardianBattleState(GuardianBase guardian, short guardianPosition, int activePlayingPlayersCount) {
        int extraHp = guardian.getHpPer() * activePlayingPlayersCount;
        int extraStr = guardian.getAddStr() * activePlayingPlayersCount;
        int extraSta = guardian.getAddSta() * activePlayingPlayersCount;
        int extraDex = guardian.getAddDex() * activePlayingPlayersCount;
        int extraWill = guardian.getAddWill() * activePlayingPlayersCount;
        int totalHp = guardian.getHpBase().shortValue() + extraHp;
        int totalStr = guardian.getBaseStr() + extraStr;
        int totalSta = guardian.getBaseSta() + extraSta;
        int totalDex = guardian.getBaseDex() + extraDex;
        int totalWill = guardian.getBaseWill() + extraWill;
        return new GuardianBattleState(guardian.getBtItemID(), guardianPosition, totalHp, totalStr, totalSta, totalDex, totalWill, guardian.getRewardExp(), guardian.getRewardGold());
    }

    private void handleAllPlayersDead(Connection connection, MatchplayGuardianGame game) {
        boolean allPlayersDead = game.getPlayerBattleStates().stream().allMatch(x -> x.getCurrentHealth() < 1);
        if (allPlayersDead && !game.isFinished()) {
            this.handleFinishGame(connection, game, false);
        }
    }

    private void handleAllGuardiansDead(Connection connection, MatchplayGuardianGame game) {
        boolean hasBossGuardianStage = game.getBossGuardianStage() != null;
        boolean allGuardiansDead = game.getGuardianBattleStates().stream().allMatch(x -> x.getCurrentHealth() < 1);
        long timePlayingInSeconds = game.getStageTimePlayingInSeconds();
        boolean triggerBossBattle = timePlayingInSeconds < game.getGuardianStage().getBossTriggerTimerInSeconds();
        if (hasBossGuardianStage && allGuardiansDead && triggerBossBattle && !game.isBossBattleActive()) {
            GameSession gameSession = connection.getClient().getActiveGameSession();
            gameSession.stopSpeedHackDetection();
            gameSession.clearCountDownRunnable();

            game.setCurrentStage(game.getBossGuardianStage());

            int activePlayingPlayersCount = game.getPlayerBattleStates().size();
            List<Byte> guardians = this.determineGuardians(game.getBossGuardianStage(), game.getGuardianLevelLimit());
            byte bossGuardianIndex = game.getBossGuardianStage().getBossGuardian().byteValue();
            game.setBossBattleActive(true);
            game.getGuardianBattleStates().clear();

            BossGuardian bossGuardian = this.bossGuardianService.findBossGuardianById((long) bossGuardianIndex);
            GuardianBattleState bossGuardianBattleState = this.createGuardianBattleState(bossGuardian, (short) 10, activePlayingPlayersCount);
            game.getGuardianBattleStates().add(bossGuardianBattleState);

            byte guardianStartPosition = 11;
            for (int i = 0; i < guardians.stream().count(); i++) {
                int guardianId = guardians.get(i);
                if (guardianId == 0) continue;

                short guardianPosition = (short) (i + guardianStartPosition);
                Guardian guardian = guardianService.findGuardianById((long) guardianId);
                GuardianBattleState guardianBattleState = this.createGuardianBattleState(guardian, guardianPosition, activePlayingPlayersCount);
                game.getGuardianBattleStates().add(guardianBattleState);
            }

            game.resetStageStartTime();

            S2CRoomSetBossGuardiansStats setBossGuardiansStats = new S2CRoomSetBossGuardiansStats(game.getGuardianBattleStates());
            this.sendPacketToAllClientsInSameGameSession(setBossGuardiansStats, connection);

            S2CMatchplaySpawnBossBattle matchplaySpawnBossBattle = new S2CMatchplaySpawnBossBattle(bossGuardianIndex, guardians.get(0), guardians.get(1));
            this.sendPacketToAllClientsInSameGameSession(matchplaySpawnBossBattle, connection);

            Runnable triggerGuardianServeRunnable = () -> {
                if (gameSession == null) return;
                int servingPositionXOffset = random.nextInt(7);
                S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, (byte) servingPositionXOffset, (byte) 0);
                S2CGameSetNameColorAndRemoveBlackBar setNameColorAndRemoveBlackBarPacket = new S2CGameSetNameColorAndRemoveBlackBar(null);
                gameSession.getClients().forEach(c -> {
                    if (c != null && c.getConnection() != null) {
                        c.getConnection().sendTCP(setNameColorAndRemoveBlackBarPacket);
                        c.getConnection().sendTCP(triggerGuardianServePacket);
                    }
                });

                gameSession.setSpeedHackCheckActive(true);
                this.startDefeatTimer(connection, game, gameSession, game.getBossGuardianStage());
            };

            RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(triggerGuardianServeRunnable, TimeUnit.SECONDS.toMillis(18));
            gameSession.getRunnableEvents().add(runnableEvent);
        } else if (allGuardiansDead && !game.isFinished()) {
            this.handleFinishGame(connection, game, true);
        }
    }

    private void startDefeatTimer(Connection connection, MatchplayGuardianGame game, GameSession gameSession, GuardianStage guardianStage) {
        if (guardianStage.getDefeatTimerInSeconds() > -1) {
            RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(() -> {
                if (game != null && !game.isFinished()) {
                    this.handleFinishGame(connection, game, false);
                }
            }, TimeUnit.SECONDS.toMillis(guardianStage.getDefeatTimerInSeconds()));

            gameSession.getRunnableEvents().add(runnableEvent);
            gameSession.setCountDownRunnable(runnableEvent);
        }
    }

    private void handleFinishGame(Connection connection, MatchplayGuardianGame game, boolean wonGame) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        game.setEndTime(cal.getTime());

        game.setFinished(true);

        List<PlayerReward> playerRewards = game.getPlayerRewards();
        playerRewards.forEach(x -> {
            int expMultiplier = game.getGuardianStage().getExpMultiplier();
            x.setBasicRewardExp(x.getBasicRewardExp() * expMultiplier);
        });

        connection.getClient().getActiveRoom().setStatus(RoomStatus.NotRunning);
        GameSession gameSession = connection.getClient().getActiveGameSession();
        gameSession.stopSpeedHackDetection();
        gameSession.clearCountDownRunnable();
        gameSession.getRunnableEvents().clear();
        gameSession.getClients().forEach(client -> {
            List<RoomPlayer> roomPlayerList = connection.getClient().getActiveRoom().getRoomPlayerList();
            RoomPlayer rp = roomPlayerList.stream()
                    .filter(x -> x.getPlayer().getId().equals(client.getActivePlayer().getId()))
                    .findFirst().orElse(null);
            if (rp == null) {
                return;
            }

            PlayerReward playerReward = playerRewards.stream()
                    .filter(x -> x.getPlayerPosition() == rp.getPosition())
                    .findFirst()
                    .orElse(this.createEmptyPlayerReward());

            Player player = client.getActivePlayer();
            byte oldLevel = player.getLevel();
            if (playerReward != null) {
                byte level = levelService.getLevel(playerReward.getBasicRewardExp(), player.getExpPoints(), player.getLevel());
                if (level != 60)
                    player.setExpPoints(player.getExpPoints() + playerReward.getBasicRewardExp());
                player.setGold(player.getGold() + playerReward.getBasicRewardGold());
                player = levelService.setNewLevelStatusPoints(level, player);
                client.setActivePlayer(player);

                if (wonGame) {
                    this.handleRewardItem(client.getConnection(), playerReward);
                }
            }

            byte playerLevel = client.getActivePlayer().getLevel();
            if (playerLevel != oldLevel) {
                StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
                rp.setStatusPointsAddedDto(statusPointsAddedDto);

                S2CGameEndLevelUpPlayerStatsPacket gameEndLevelUpPlayerStatsPacket = new S2CGameEndLevelUpPlayerStatsPacket(rp.getPosition(), player, rp.getStatusPointsAddedDto());
                packetEventHandler.push(packetEventHandler.createPacketEvent(client, gameEndLevelUpPlayerStatsPacket, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);
            }

            if (wonGame) {
                S2CMatchplayDisplayItemRewards s2CMatchplayDisplayItemRewards = new S2CMatchplayDisplayItemRewards(playerRewards);
                client.getConnection().sendTCP(s2CMatchplayDisplayItemRewards);
            }

            byte resultTitle = (byte) (wonGame ? 1 : 0);
            S2CMatchplaySetExperienceGainInfoData setExperienceGainInfoData = new S2CMatchplaySetExperienceGainInfoData(resultTitle, (int) Math.ceil((double) game.getTimeNeeded() / 1000), playerReward, playerLevel);
            client.getConnection().sendTCP(setExperienceGainInfoData);

            S2CMatchplaySetGameResultData setGameResultData = new S2CMatchplaySetGameResultData(playerRewards);
            client.getConnection().sendTCP(setGameResultData);

            S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
            packetEventHandler.push(packetEventHandler.createPacketEvent(client, backToRoomPacket, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(12)), PacketEventHandler.ServerClient.SERVER);
            client.setActiveGameSession(null);
        });

        gameSession.getClients().removeIf(c -> c.getActiveGameSession() == null);
        if (game.isFinished() && gameSession.getClients().isEmpty()) {
            this.gameSessionManager.removeGameSession(gameSession);
        }
    }

    private void handleRewardItem(Connection connection, PlayerReward playerReward) {
        if (playerReward.getRewardProductIndex() < 0) {
            return;
        }

        Product product = this.productService.findProductByProductItemIndex(playerReward.getRewardProductIndex());
        if (product == null) {
            return;
        }

        Player player = connection.getClient().getActivePlayer();
        Pocket pocket = player.getPocket();
        PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(product.getItem0(), product.getCategory(), pocket);
        int existingItemCount = 0;
        boolean existingItem = false;

        if (playerPocket != null && !playerPocket.getUseType().equals("N/A")) {
            existingItemCount = playerPocket.getItemCount();
            existingItem = true;
        } else {
            playerPocket = new PlayerPocket();
        }

        playerPocket.setCategory(product.getCategory());
        playerPocket.setItemIndex(product.getItem0());
        playerPocket.setUseType(product.getUseType());

        playerPocket.setItemCount(product.getUse0() == 0 ? 1 : product.getUse0());

        // no idea how itemCount can be null here, but ok
        playerPocket.setItemCount((playerPocket.getItemCount() == null ? 0 : playerPocket.getItemCount()) + existingItemCount);

        if (playerPocket.getUseType().equalsIgnoreCase(EItemUseType.TIME.getName())) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.add(Calendar.DAY_OF_MONTH, playerPocket.getItemCount());

            playerPocket.setCreated(cal.getTime());
        }
        playerPocket.setPocket(pocket);

        playerPocketService.save(playerPocket);
        if (!existingItem)
            pocket = pocketService.incrementPocketBelongings(pocket);

        // add item to result
        connection.getClient().getActivePlayer().setPocket(pocket);

        List<PlayerPocket> playerPocketList = new ArrayList<>();
        playerPocketList.add(playerPocket);

        S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(playerPocketList);
        connection.sendTCP(inventoryDataPacket);
    }

    private PlayerReward createEmptyPlayerReward() {
        PlayerReward playerReward = new PlayerReward();
        playerReward.setBasicRewardExp(1);
        playerReward.setBasicRewardGold(1);
        return playerReward;
    }

    private void handleMonsLavaMap(Connection connection, Room room, float averagePlayerLevel) {
        boolean isMonsLava = room.getMap() == 7 || room.getMap() == 8;
        Random random = new Random();
        int monsLavaBProbability = random.nextInt(101);
        if (isMonsLava && averagePlayerLevel >= 40 && monsLavaBProbability <= 26) {
            room.setMap((byte) 8); // MonsLavaB
            S2CRoomMapChangeAnswerPacket roomMapChangeAnswerPacket = new S2CRoomMapChangeAnswerPacket(room.getMap());
            this.sendPacketToAllClientsInSameGameSession(roomMapChangeAnswerPacket, connection);
        } else if (room.getMap() == 8) {
            room.setMap((byte) 7); // MonsLava
            S2CRoomMapChangeAnswerPacket roomMapChangeAnswerPacket = new S2CRoomMapChangeAnswerPacket(room.getMap());
            this.sendPacketToAllClientsInSameGameSession(roomMapChangeAnswerPacket, connection);
        }
    }

    private List<Byte> determineGuardians(GuardianStage guardianStage, int guardianLevelLimit) {
        List<Guardian> guardiansLeft = this.getFilteredGuardians(guardianStage.getGuardiansLeft(), guardianLevelLimit);
        List<Guardian> guardiansRight = this.getFilteredGuardians(guardianStage.getGuardiansRight(), guardianLevelLimit);
        List<Guardian> guardiansMiddle = this.getFilteredGuardians(guardianStage.getGuardiansMiddle(), guardianLevelLimit);

        byte leftGuardian = this.getRandomGuardian(guardiansLeft, new ArrayList<>());
        byte rightGuardian = this.getRandomGuardian(guardiansRight, Arrays.asList(leftGuardian));
        byte middleGuardian = this.getRandomGuardian(guardiansMiddle, Arrays.asList(leftGuardian, rightGuardian));
        if (middleGuardian != 0) {
            return Arrays.asList(middleGuardian, rightGuardian, leftGuardian);
        } else if (rightGuardian != 0) {
            return Arrays.asList(rightGuardian, leftGuardian, (byte) 0);
        }

        return Arrays.asList(leftGuardian, (byte) 0, (byte) 0);
    }

    private byte getRandomGuardian(List<Guardian> guardians, List<Byte> idsToIgnore) {
        byte guardianId = 0;
        if (guardians.size() > 0) {
            int amountsOfGuardiansToChooseFrom = guardians.size();
            if (amountsOfGuardiansToChooseFrom == 1) {
                return guardians.get(0).getId().byteValue();
            }

            Random r = new Random();
            int guardianIndex = r.nextInt(amountsOfGuardiansToChooseFrom);
            guardianId = guardians.get(guardianIndex).getId().byteValue();
            if (idsToIgnore.contains(guardianId)) {
                return getRandomGuardian(guardians, idsToIgnore);
            }
        }

        return guardianId;
    }

    private List<Guardian> getFilteredGuardians(List<Integer> ids, int guardianLevelLimit) {
        if (ids == null) {
            return new ArrayList<>();
        }

        List<Guardian> guardians = this.guardianService.findGuardiansByIds(ids);
        int lowestGuardianLevel = guardians.stream().min(Comparator.comparingInt(x -> x.getLevel())).get().getLevel();
        if (guardianLevelLimit < lowestGuardianLevel) {
            guardianLevelLimit = lowestGuardianLevel;
        }

        final int finalGuardianLevelLimit = guardianLevelLimit;
        return this.guardianService.findGuardiansByIds(ids).stream()
                .filter(x -> x.getLevel() <= finalGuardianLevelLimit)
                .collect(Collectors.toList());
    }

    private float getAveragePlayerLevel(List<RoomPlayer> roomPlayers) {
        List<RoomPlayer> activePlayingPlayers = roomPlayers.stream().filter(x -> x.getPosition() < 4).collect(Collectors.toList());
        List<Integer> playerLevels = activePlayingPlayers.stream().map(x -> (int) x.getPlayer().getLevel()).collect(Collectors.toList());
        int levelSum = playerLevels.stream().reduce(0, Integer::sum);
        float averagePlayerLevel = levelSum / activePlayingPlayers.size();
        return averagePlayerLevel;
    }

    private int getGuardianLevelLimit(float averagePlayerLevel) {
        int minGuardianLevelLimit = 10;
        int roundLevel = 5 * (Math.round(averagePlayerLevel / 5));
        if (roundLevel < averagePlayerLevel) {
            if (averagePlayerLevel < minGuardianLevelLimit) return minGuardianLevelLimit;
            return (int) averagePlayerLevel;
        }

        if (roundLevel < minGuardianLevelLimit) return minGuardianLevelLimit;
        return roundLevel;
    }

    private void handleRevivePlayer(Connection connection, MatchplayGuardianGame game, Skill skill, C2SMatchplaySkillHitsTarget skillHitsTarget) {
        PlayerBattleState playerBattleState = null;
        try {
            playerBattleState = game.reviveAnyPlayer(skill.getDamage().shortValue());
        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
            return;
        }
        if (playerBattleState != null) {
            S2CMatchplayDealDamage damageToPlayerPacket =
                    new S2CMatchplayDealDamage(playerBattleState.getPosition(), playerBattleState.getCurrentHealth(), (short) 0, skillHitsTarget.getSkillId(), skillHitsTarget.getXKnockbackPosition(), skillHitsTarget.getYKnockbackPosition());
            this.sendPacketToAllClientsInSameGameSession(damageToPlayerPacket, connection);
        }
    }

    private void handleReviveGuardian(Connection connection, MatchplayGuardianGame game, Skill skill) {
        GuardianBattleState guardianBattleState = null;
        try {
            guardianBattleState = game.reviveAnyGuardian(skill.getDamage().shortValue());
        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
            return;
        }
        if (guardianBattleState != null) {
            S2CMatchplayDealDamage damageToPlayerPacket =
                    new S2CMatchplayDealDamage(guardianBattleState.getPosition(), (short) guardianBattleState.getCurrentHealth(), (short) 0, skill.getId().byteValue(), 0, 0);
            this.sendPacketToAllClientsInSameGameSession(damageToPlayerPacket, connection);
        }
    }

    private void placeCrystalRandomly(Connection connection, MatchplayGuardianGame game) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession == null) return;
        Point2D point = this.getRandomPoint();

        short crystalId = (short) (game.getLastCrystalId() + 1);
        game.setLastCrystalId(crystalId);
        SkillCrystal skillCrystal = new SkillCrystal();
        skillCrystal.setId(crystalId);
        game.getSkillCrystals().add(skillCrystal);

        S2CMatchplayPlaceSkillCrystal placeSkillCrystal = new S2CMatchplayPlaceSkillCrystal(skillCrystal.getId(), point);
        this.sendPacketToAllClientsInSameGameSession(placeSkillCrystal, connection);

        Runnable despawnCrystalRunnable = () -> {
            if (gameSession == null) return;
            boolean isCrystalStillAvailable = game.getSkillCrystals().stream().anyMatch(x -> x.getId() == skillCrystal.getId());
            if (isCrystalStillAvailable) {
                S2CMatchplayLetCrystalDisappear letCrystalDisappearPacket = new S2CMatchplayLetCrystalDisappear(skillCrystal.getId());
                this.sendPacketToAllClientsInSameGameSession(letCrystalDisappearPacket, connection);
                game.getSkillCrystals().remove(skillCrystal);
                RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(() -> this.placeCrystalRandomly(connection, game), game.getCrystalSpawnInterval());
                gameSession.getRunnableEvents().add(runnableEvent);
            }
        };

        RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(despawnCrystalRunnable, game.getCrystalDeSpawnInterval());
        gameSession.getRunnableEvents().add(runnableEvent);
    }

    private void handleQuickSlotItemUse(Connection connection, C2SMatchplayUsesSkill playerUseSkill) {
        Player player = connection.getClient().getActivePlayer();
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

    private Point2D getRandomPoint() {
        int negator = (int) (Math.random() * 2) == 0 ? -1 : 1;
        float xPos = (float) (Math.random() * 60) * negator;
        float yPos = (short) (Math.random() * 120) * -1;
        yPos = Math.abs(yPos) < 10 ? -10 : yPos;
        return new Point2D.Float(xPos, yPos);
    }

    private int getRandomGuardianSkillBasedOnProbability(int btItemId) {
        GuardianBtItemList guardianBtItemList = this.guardianSkillsService.findGuardianBtItemListById(btItemId);
        List<Integer> dropRatesInt = guardianBtItemList.getGuardianBtItems().stream().map(x -> x.getChance()).collect(Collectors.toList());

        List<SkillDrop> skillDrops = new ArrayList<>();
        int currentPercentage = 0;
        for (int i = 0; i < dropRatesInt.size(); i++) {
            int item = dropRatesInt.get(i);
            if (item != 0) {
                SkillDrop skillDrop = new SkillDrop();
                skillDrop.setId(i);
                skillDrop.setFrom(currentPercentage);
                skillDrop.setTo(currentPercentage + item);
                skillDrops.add(skillDrop);
                currentPercentage += item;
            }
        }

        Random random = new Random();
        int randValue = random.nextInt(101);
        SkillDrop skillDrop = skillDrops.stream().filter(x -> x.getFrom() <= randValue && x.getTo() >= randValue).findFirst().orElse(null);
        GuardianBtItem guardianBtItem = guardianBtItemList.getGuardianBtItems().get(skillDrop.getId());
        return guardianBtItem.getSkillIndex();
    }

    private RoomPlayer getRoomPlayerFromConnection(Connection connection) {
        RoomPlayer roomPlayer = connection.getClient().getActiveRoom().getRoomPlayerList().stream()
                .filter(x -> x.getPlayer().getId() == connection.getClient().getActivePlayer().getId())
                .findFirst()
                .orElse(null);
        return roomPlayer;
    }

    private void sendPacketToAllClientsInSameGameSession(Packet packet, Connection connection) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession != null) {
            List<Client> clientsInGameSession = new ArrayList<>();
            clientsInGameSession.addAll(gameSession.getClients()); // deep copy
            clientsInGameSession.forEach(c -> c.getConnection().sendTCP(packet));
        }
    }
}
