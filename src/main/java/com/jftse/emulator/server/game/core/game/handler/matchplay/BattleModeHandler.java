package com.jftse.emulator.server.game.core.game.handler.matchplay;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.database.model.battle.Skill;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.PlayerStatistic;
import com.jftse.emulator.server.database.model.player.QuickSlotEquipment;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.database.model.pocket.Pocket;
import com.jftse.emulator.server.game.core.constants.GameFieldSide;
import com.jftse.emulator.server.game.core.constants.GameMode;
import com.jftse.emulator.server.game.core.constants.PacketEventType;
import com.jftse.emulator.server.game.core.constants.RoomStatus;
import com.jftse.emulator.server.game.core.item.EItemCategory;
import com.jftse.emulator.server.game.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.game.core.matchplay.PlayerReward;
import com.jftse.emulator.server.game.core.matchplay.basic.MatchplayBattleGame;
import com.jftse.emulator.server.game.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.game.core.matchplay.battle.SkillCrystal;
import com.jftse.emulator.server.game.core.matchplay.event.PacketEventHandler;
import com.jftse.emulator.server.game.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.game.core.matchplay.event.RunnableEventHandler;
import com.jftse.emulator.server.game.core.matchplay.room.GameSession;
import com.jftse.emulator.server.game.core.matchplay.room.PlayerPositionInfo;
import com.jftse.emulator.server.game.core.matchplay.room.Room;
import com.jftse.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.game.core.packet.packets.authserver.S2CLoginAnswerPacket;
import com.jftse.emulator.server.game.core.packet.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.game.core.packet.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.emulator.server.game.core.packet.packets.matchplay.*;
import com.jftse.emulator.server.game.core.service.*;
import com.jftse.emulator.server.game.core.utils.BattleUtils;
import com.jftse.emulator.server.game.core.utils.RankingUtils;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;
import com.jftse.emulator.server.shared.module.GameHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class BattleModeHandler {
    private final PacketEventHandler packetEventHandler;
    private final RunnableEventHandler runnableEventHandler;
    private final SkillService skillService;
    private final PlayerPocketService playerPocketService;
    private final PlayerService playerService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;
    private final PocketService pocketService;
    private final LevelService levelService;
    private final ClothEquipmentService clothEquipmentService;
    private final GameSessionManager gameSessionManager;
    private final WillDamageService willDamageService;
    private final PlayerStatisticService playerStatisticService;
    private final AuthenticationService authenticationService;

    private GameHandler gameHandler;

    private Random random;

    public void init(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.random = new Random();
    }

    public void handleBattleModeMatchplayPointPacket(Connection connection, C2SMatchplayPointPacket matchplayPointPacket, GameSession gameSession, MatchplayBattleGame game) {
        boolean lastGuardianServeWasOnBlueTeamsSide = game.getLastGuardianServeSide() == GameFieldSide.BlueTeam;
        int servingPositionXOffset = random.nextInt(7);
        if (!lastGuardianServeWasOnBlueTeamsSide) {
            game.setLastGuardianServeSide(GameFieldSide.BlueTeam);
            S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe((byte) GameFieldSide.BlueTeam, (byte) servingPositionXOffset, (byte) 0);
            gameSession.getClients().forEach(x -> x.getConnection().sendTCP(triggerGuardianServePacket));
        } else {
            game.setLastGuardianServeSide(GameFieldSide.RedTeam);
            S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe((byte) GameFieldSide.RedTeam, (byte) servingPositionXOffset, (byte) 0);
            gameSession.getClients().forEach(x -> x.getConnection().sendTCP(triggerGuardianServePacket));
        }
    }

    public void handleStartBattleMode(Connection connection, Room room) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayBattleGame game = (MatchplayBattleGame) gameSession.getActiveMatchplayGame();
        game.setLastGuardianServeSide(GameFieldSide.RedTeam);
        List<Client> clients = this.gameHandler.getClientsInRoom(room.getRoomId());

        List<PlayerPositionInfo> positionInfo = new ArrayList<>();
        clients.forEach(c -> {
            RoomPlayer rp = room.getRoomPlayerList().stream()
                    .filter(x -> x.getPlayer().getId().equals(c.getActivePlayer().getId()))
                    .findFirst().orElse(null);

            Point playerLocation = game.getPlayerLocationsOnMap().get(rp.getPosition());
            PlayerPositionInfo playerPositionInfo = new PlayerPositionInfo();
            playerPositionInfo.setPlayerPosition(rp.getPosition());
            playerPositionInfo.setPlayerStartLocation(playerLocation);
            positionInfo.add(playerPositionInfo);
        });

        int servingPositionXOffset = random.nextInt(7);

        S2CMatchplaySetPlayerPosition setPlayerPositionPacket = new S2CMatchplaySetPlayerPosition(positionInfo);
        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe((byte) GameFieldSide.RedTeam, (byte) servingPositionXOffset, (byte) 0);
        clients.forEach(c -> {
            c.getConnection().sendTCP(setPlayerPositionPacket);
            c.getConnection().sendTCP(triggerGuardianServePacket);
        });

        long crystalSpawnInterval = TimeUnit.SECONDS.toMillis(8);
        long crystalDeSpawnInterval = TimeUnit.SECONDS.toMillis(10);
        game.setCrystalSpawnInterval(crystalSpawnInterval);
        game.setCrystalDeSpawnInterval(crystalDeSpawnInterval);

        int activePlayers = (int) game.getPlayerBattleStates().stream().count();
        int amountOfCrystalsToSpawnPerSide = activePlayers > 2 ? 2 : 1;
        Runnable initializeCrystalsRunnable = () -> {
            for (int i = 0; i < amountOfCrystalsToSpawnPerSide; i++) {
                this.placeCrystalRandomly(connection, game, GameFieldSide.RedTeam);
                this.placeCrystalRandomly(connection, game, GameFieldSide.BlueTeam);
            }
        };
        
        RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(initializeCrystalsRunnable, crystalSpawnInterval);
        gameSession.getRunnableEvents().add(runnableEvent);
        // gameSession.setSpeedHackCheckActive(true);
    }

    public void handlePrepareBattleMode(Connection connection, Room room) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayBattleGame game = (MatchplayBattleGame) gameSession.getActiveMatchplayGame();
        game.setWillDamages(this.willDamageService.getWillDamages());

        List<RoomPlayer> roomPlayers = room.getRoomPlayerList();
        roomPlayers.forEach(rp -> {
            if (rp.getPosition() < 4)
                game.getPlayerBattleStates().add(this.createPlayerBattleState(rp));
        });
    }

    public void handlePlayerPickingUpCrystal(Connection connection, C2SMatchplayPlayerPicksUpCrystal playerPicksUpCrystalPacket) {
        // sometimes we are faster when cleaning up game sessions till the player is thrown back to the room
        if (connection.getClient().getActiveGameSession() == null) return;
        
        RoomPlayer roomPlayer = this.getRoomPlayerFromConnection(connection);
        short playerPosition = roomPlayer.getPosition();
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayBattleGame game = (MatchplayBattleGame) gameSession.getActiveMatchplayGame();
        SkillCrystal skillCrystal = game.getSkillCrystals().stream()
                .filter(x -> x.getId() == playerPicksUpCrystalPacket.getCrystalId())
                .findFirst()
                .orElse(null);

        if (skillCrystal != null) {
            if (gameSession == null) return;
            short gameFieldSide = game.isRedTeam(playerPosition) ? GameFieldSide.RedTeam : GameFieldSide.BlueTeam;
            S2CMatchplayGiveRandomSkill randomSKill =
                    new S2CMatchplayGiveRandomSkill(playerPicksUpCrystalPacket.getCrystalId(), (byte) playerPosition);
            this.sendPacketToAllClientsInSameGameSession(randomSKill, connection);

            game.getSkillCrystals().remove(skillCrystal);
            RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(() -> this.placeCrystalRandomly(connection, game, gameFieldSide), game.getCrystalSpawnInterval());
            gameSession.getRunnableEvents().add(runnableEvent);
        }
    }

    public void handleUseOfSkill(Connection connection, C2SMatchplayUsesSkill anyoneUsesSkill) {
        byte position = anyoneUsesSkill.getAttackerPosition();
        GameSession gameSession = connection.getClient().getActiveGameSession();
        // sometimes we are faster when cleaning up game sessions till the player is thrown back to the room
        if (gameSession == null) return;

        MatchplayBattleGame game = (MatchplayBattleGame) gameSession.getActiveMatchplayGame();
        if (game == null) return;

        Room room = connection.getClient().getActiveRoom();
        List<RoomPlayer> roomPlayers = room.getRoomPlayerList();
        RoomPlayer roomPlayer = roomPlayers.stream()
                .filter(x -> x.getPlayer() != null && x.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                .findAny()
                .orElse(null);
        Skill skill = skillService.findSkillById((long) anyoneUsesSkill.getSkillIndex() + 1);

        if (roomPlayer != null) {
            PlayerBattleState playerBattleState = game.getPlayerBattleStates().stream()
                    .filter(x -> x.getPosition() == roomPlayer.getPosition())
                    .findFirst()
                    .orElse(null);
            if (anyoneUsesSkill.isQuickSlot()) {
                if (playerBattleState != null) {
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

                    if (playerBattleState.getLastQS().containsKey(skill.getId())) {
                        long lastQSUseTime = playerBattleState.getLastQS().get(skill.getId());
                        lastQSUseTime -= connection.getLatency();
                        long timePassed = cal.getTimeInMillis() - lastQSUseTime;
                        if (timePassed >= skill.getCoolingTime().longValue()) {
                            this.handleQuickSlotItemUse(connection, anyoneUsesSkill);
                            playerBattleState.getLastQS().put(skill.getId(), cal.getTimeInMillis());
                        } else {
                            playerBattleState.setCurrentHealth((short) 0);
                            playerBattleState.setDead(true);
                            S2CMatchplayDealDamage matchplayDealDamage = new S2CMatchplayDealDamage((short) position, playerBattleState.getCurrentHealth(), skill.getTargeting().shortValue(), (byte) 3, 0, 0);
                            S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", roomPlayer.getPlayer().getName() + " died because of no QS CD hack. Marked for ban.");
                            this.sendPacketToAllClientsInSameGameSession(matchplayDealDamage, connection);
                            this.sendPacketToAllClientsInSameGameSession(chatRoomAnswerPacket, connection);

                            Account account = authenticationService.findAccountById(connection.getClient().getAccount().getId());
                            account.setStatus((int) S2CLoginAnswerPacket.ACCOUNT_BLOCKED_USER_ID);
                            this.authenticationService.updateAccount(account);
                            return;
                        }

                    } else {
                        this.handleQuickSlotItemUse(connection, anyoneUsesSkill);
                        playerBattleState.getLastQS().put(skill.getId(), cal.getTimeInMillis());
                    }
                }
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
        // if (!gameSession.isSpeedHackCheckActive()) return;

        MatchplayBattleGame game = (MatchplayBattleGame) gameSession.getActiveMatchplayGame();
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

        this.handleAnyTeamDead(connection, game);
    }

    private void handleAnyTeamDead(Connection connection, MatchplayBattleGame game) {
        boolean allPlayersTeamRedDead = game.getPlayerBattleStates().stream().filter(x -> game.isRedTeam(x.getPosition())).allMatch(x -> x.getCurrentHealth() < 1);
        boolean allPlayersTeamBlueDead = game.getPlayerBattleStates().stream().filter(x -> game.isBlueTeam(x.getPosition())).allMatch(x -> x.getCurrentHealth() < 1);

        if ((allPlayersTeamRedDead || allPlayersTeamBlueDead) && !game.isFinished()) {
            this.handleFinishGame(connection, game);
        }
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

    private void handleBallLossDamage(Connection connection, C2SMatchplaySkillHitsTarget skillHitsTarget) {
        short receiverPosition = skillHitsTarget.getTargetPosition();
        short attackerPosition = skillHitsTarget.getAttackerPosition();
        boolean attackerHasWillBuff = skillHitsTarget.getAttackerBuffId() == 3;

        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayBattleGame game = (MatchplayBattleGame) gameSession.getActiveMatchplayGame();

        short newHealth;
        try {
            newHealth = game.damagePlayerOnBallLoss(receiverPosition, attackerPosition, attackerHasWillBuff);
        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
            return;
        }

        S2CMatchplayDealDamage damageToPlayerPacket = new S2CMatchplayDealDamage(skillHitsTarget.getTargetPosition(), newHealth, (short) 0, (byte) 0, 0, 0);
        this.sendPacketToAllClientsInSameGameSession(damageToPlayerPacket, connection);
    }

    private void handleSkillDamage(Connection connection, short targetPosition, C2SMatchplaySkillHitsTarget skillHitsTarget, MatchplayBattleGame game, Skill skill) {
        boolean denyDamage = skillHitsTarget.getDamageType() == 1;
        short attackerPosition = skillHitsTarget.getAttackerPosition();
        boolean attackerHasStrBuff = skillHitsTarget.getAttackerBuffId() == 0;
        boolean receiverHasDefBuff = skillHitsTarget.getReceiverBuffId() == 1;

        short skillDamage = skill != null ? skill.getDamage().shortValue() : -1;
        short newHealth = 0;
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

    private boolean isUniqueSkill(Skill skill) {
        int skillId = skill.getId().intValue();
        return skillId == 5 || skillId == 38;
    }

    private void handleUniqueSkill(Connection connection, MatchplayBattleGame game, Skill skill, C2SMatchplaySkillHitsTarget skillHitsTarget) {
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
    
    private void handleFinishGame(Connection connection, MatchplayBattleGame game) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        game.setEndTime(cal.getTime());

        game.setFinished(true);

        List<PlayerReward> playerRewards = game.getPlayerRewards();
        connection.getClient().getActiveRoom().setStatus(RoomStatus.NotRunning);
        GameSession gameSession = connection.getClient().getActiveGameSession();
        // gameSession.stopSpeedHackDetection();
        gameSession.clearCountDownRunnable();
        gameSession.getRunnableEvents().clear();

        List<Client> clients = new ArrayList<>(gameSession.getClients());
        List<Player> playerList = new ArrayList<>();
        clients.forEach(c -> playerList.add(c.getActivePlayer()));

        for (Client client : clients) {
            List<RoomPlayer> roomPlayerList = connection.getClient().getActiveRoom().getRoomPlayerList();
            RoomPlayer rp = roomPlayerList.stream()
                    .filter(x -> x.getPlayer().getId().equals(client.getActivePlayer().getId()))
                    .findFirst().orElse(null);
            if (rp == null) {
                return;
            }

            boolean isCurrentPlayerInRedTeam = game.isRedTeam(rp.getPosition());
            boolean allPlayersTeamRedDead = game.getPlayerBattleStates().stream().filter(x -> game.isRedTeam(x.getPosition())).allMatch(x -> x.getCurrentHealth() < 1);
            boolean allPlayersTeamBlueDead = game.getPlayerBattleStates().stream().filter(x -> game.isBlueTeam(x.getPosition())).allMatch(x -> x.getCurrentHealth() < 1);
            boolean wonGame = false;
            if (isCurrentPlayerInRedTeam && allPlayersTeamBlueDead || !isCurrentPlayerInRedTeam && allPlayersTeamRedDead) {
                wonGame = true;
            }

            PlayerReward playerReward = playerRewards.stream()
                    .filter(x -> x.getPlayerPosition() == rp.getPosition())
                    .findFirst()
                    .orElse(this.createEmptyPlayerReward());

            Player player = client.getActivePlayer();
            byte oldLevel = player.getLevel();
            if (playerReward != null) {
                byte level = levelService.getLevel(playerReward.getRewardExp(), player.getExpPoints(), player.getLevel());
                if (level != 60)
                    player.setExpPoints(player.getExpPoints() + playerReward.getRewardExp());
                player.setGold(player.getGold() + playerReward.getRewardGold());
                player = levelService.setNewLevelStatusPoints(level, player);
                client.setActivePlayer(player);

                if (wonGame) {
                    this.handleRewardItem(client.getConnection(), playerReward);
                }
            }

            PlayerStatistic playerStatistic = player.getPlayerStatistic();
            if (wonGame) {
                playerStatistic.setBattleRecordWin(playerStatistic.getBattleRecordWin() + 1);

                int newCurrentConsecutiveWins = playerStatistic.getConsecutiveWins() + 1;
                if (newCurrentConsecutiveWins > playerStatistic.getMaxConsecutiveWins()) {
                    playerStatistic.setMaxConsecutiveWins(newCurrentConsecutiveWins);
                }

                playerStatistic.setConsecutiveWins(newCurrentConsecutiveWins);
            } else {
                playerStatistic.setBattleRecordLoss(playerStatistic.getBattleRecordLoss() + 1);
                playerStatistic.setConsecutiveWins(0);
            }
            HashMap<Long, Integer> playerRatings = RankingUtils.calculateNewRating(playerList, player, wonGame, (byte) GameMode.BATTLE);
            int playerRankingPoints = playerRatings.get(player.getId()) - playerStatistic.getBattleRP();
            int playerNewRating = playerRatings.get(player.getId());
            if (playerReward != null)
                playerReward.setRewardRP(playerRankingPoints);
            playerStatistic.setBattleRP(playerNewRating <= 0 ? 0 : playerNewRating);

            playerStatistic = playerStatisticService.save(player.getPlayerStatistic());

            player.setPlayerStatistic(playerStatistic);
            player = playerService.save(player);
            client.setActivePlayer(player);

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
        }

        gameSession.getClients().removeIf(c -> c.getActiveGameSession() == null);
        if (game.isFinished() && gameSession.getClients().isEmpty()) {
            this.gameSessionManager.removeGameSession(gameSession);
        }
    }

    private void handleRewardItem(Connection connection, PlayerReward playerReward) {
    }

    private PlayerReward createEmptyPlayerReward() {
        PlayerReward playerReward = new PlayerReward();
        playerReward.setRewardExp(1);
        playerReward.setRewardGold(1);
        return playerReward;
    }

    private void handleRevivePlayer(Connection connection, MatchplayBattleGame game, Skill skill, C2SMatchplaySkillHitsTarget skillHitsTarget) {
        PlayerBattleState playerBattleState = null;

        Optional<RoomPlayer> rp = connection.getClient().getActiveRoom().getRoomPlayerList().stream()
                .filter(p -> p.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                .findFirst();

        try {
            playerBattleState = game.reviveAnyPlayer(skill.getDamage().shortValue(), rp);
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

    private void placeCrystalRandomly(Connection connection, MatchplayBattleGame game, short gameFieldSide) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession == null) return;
        Point2D point = this.getRandomPoint(gameFieldSide);

        short crystalId = (short) (game.getLastCrystalId() + 1);
        if (crystalId > 100) crystalId = 0;
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
                RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(() -> this.placeCrystalRandomly(connection, game, gameFieldSide), game.getCrystalSpawnInterval());
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

    private Point2D getRandomPoint(short gameFieldSide) {
        int negator = (int) (Math.random() * 2) == 0 ? -1 : 1;
        float xPos = (float) (Math.random() * 60) * negator;

        float yPos = (short) (Math.random() * 120);
        if (gameFieldSide == GameFieldSide.RedTeam) {
            yPos = (short) (Math.random() * 120) * -1;
            yPos = Math.abs(yPos) < 10 ? -10 : yPos;
        } else if (gameFieldSide == GameFieldSide.BlueTeam) {
            yPos = Math.abs(yPos) < 10 ? 10 : yPos;
        }

        return new Point2D.Float(xPos, yPos);
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
        gameSession.getClients().forEach(c -> c.getConnection().sendTCP(packet));
    }
}
