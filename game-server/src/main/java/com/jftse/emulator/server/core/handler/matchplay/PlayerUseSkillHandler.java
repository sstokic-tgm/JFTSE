package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.client.EquippedQuickSlots;
import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.life.event.GameEventBus;
import com.jftse.emulator.server.core.life.event.GameEventType;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayDealDamage;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.battle.Skill;
import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.model.log.GameLogType;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.server.core.matchplay.battle.SkillCrystal;
import com.jftse.server.core.matchplay.battle.SkillUse;
import com.jftse.server.core.service.*;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.packets.S2CDCMsgPacket;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.server.core.shared.packets.matchplay.CMSGPlayerUseSkill;
import com.jftse.server.core.shared.packets.matchplay.SMSGPlayerUseSkill;
import com.jftse.server.core.util.Time;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@Log4j2
@PacketId(CMSGPlayerUseSkill.PACKET_ID)
public class PlayerUseSkillHandler implements PacketHandler<FTConnection, CMSGPlayerUseSkill> {
    private final SkillService skillService;
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;
    private final AuthenticationService authenticationService;

    private final GameLogService gameLogService;

    private static final int TIMESTAMP_DELTA = 500;

    public PlayerUseSkillHandler() {
        this.skillService = ServiceManager.getInstance().getSkillService();
        this.pocketService = ServiceManager.getInstance().getPocketService();
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.quickSlotEquipmentService = ServiceManager.getInstance().getQuickSlotEquipmentService();
        this.authenticationService = ServiceManager.getInstance().getAuthenticationService();
        this.gameLogService = ServiceManager.getInstance().getGameLogService();
    }

    @Override
    public void handle(FTConnection connection, CMSGPlayerUseSkill anyoneUsesSkill) {
        FTClient ftClient = connection.getClient();
        if (!ftClient.hasPlayer() || ftClient.getActiveGameSession() == null || ftClient.getRoomPlayer() == null)
            return;

        long skillUseTimestamp = Time.nanoToMillis(Time.getNSTime()) + TIMESTAMP_DELTA;
        FTPlayer player = ftClient.getPlayer();
        RoomPlayer roomPlayer = ftClient.getRoomPlayer();
        GameSession gameSession = ftClient.getActiveGameSession();

        MatchplayGame game = gameSession.getMatchplayGame();
        if (game == null) return;

        byte attackerPosition = anyoneUsesSkill.getAttackerPosition();
        byte targetPosition = anyoneUsesSkill.getTargetPosition();
        boolean isQuickSlot = anyoneUsesSkill.getIsQuickSlot();

        boolean attackerIsGuardian = attackerPosition > 9;
        boolean attackerIsPlayer = attackerPosition < 4;

        if (attackerIsPlayer && !isQuickSlot) {
            // a canceled skill use will have sourceValue -1 and skillIndex -6?, a less than zero check is enough
            if (anyoneUsesSkill.getSourceValue() == -1 && anyoneUsesSkill.getSkillIndex() < 0) {
                roomPlayer.getPickedUpSkillCrystals().poll();
                return;
            }

            try {
                validateSkillCrystal(roomPlayer.getPickedUpSkillCrystals(), anyoneUsesSkill.getSourceValue(), anyoneUsesSkill.getSkillIndex());
            } catch (ValidationException ve) {
                log.warn("({}) {}", player.getId(), ve.getMessage());
                return;
            }
        }

        Skill skill = skillService.findSkillByIndex(anyoneUsesSkill.getSkillIndex());
        SkillUse skillUse = null;
        if (skill != null)
            skillUse = new SkillUse(skill, attackerPosition, targetPosition, isQuickSlot, skillUseTimestamp, false);

        GameEventBus.call(GameEventType.MP_PLAYER_USE_SKILL, ftClient, game, roomPlayer, skill, skillUse, anyoneUsesSkill);

        if (attackerIsGuardian) {
            if (skill != null) {
                this.handleSpecialSkillsUseOfGuardians(connection, attackerPosition, (MatchplayGuardianGame) game, skill);
            }
        } else if (attackerIsPlayer) {
            if (roomPlayer != null) {
                if (isQuickSlot) {
                    if (!isQsUseValid(connection, skillUseTimestamp, player, skill, skillUse, game, attackerPosition, anyoneUsesSkill))
                        return;

                    try {
                        this.handleQuickSlotItemUse(connection, player, anyoneUsesSkill);
                    } catch (ValidationException ve) {
                        log.warn("({}) {}", player.getId(), ve.getMessage());
                        return;
                    }
                }
            }
        }

        SMSGPlayerUseSkill response = SMSGPlayerUseSkill.builder()
                .attacker(attackerPosition)
                .target(targetPosition)
                .skillId(anyoneUsesSkill.getSkillIndex())
                .seed(anyoneUsesSkill.getSeed())
                .xTarget(anyoneUsesSkill.getXTarget())
                .zTarget(anyoneUsesSkill.getZTarget())
                .yTarget(anyoneUsesSkill.getYTarget())
                .build();
        gameSession.getClients().forEach(c -> {
            if (c.getConnection().getId() != connection.getId()) {
                c.getConnection().sendTCP(response);
            }
        });
    }

    private boolean isQsUseValid(FTConnection connection, long skillUseTimestamp, FTPlayer player, Skill skill, SkillUse skillUse, MatchplayGame game, byte attackerPosition, CMSGPlayerUseSkill anyoneUsesSkill) {
        PlayerBattleState playerBattleState;
        if (game instanceof MatchplayGuardianGame) {
            playerBattleState = ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                    .filter(pbs -> pbs.getPosition() == attackerPosition)
                    .findFirst()
                    .orElse(null);
        } else {
            playerBattleState = ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                    .filter(pbs -> pbs.getPosition() == attackerPosition)
                    .findFirst()
                    .orElse(null);
        }
        if (playerBattleState != null && skillUse != null) {
            int quickSlotIndex = anyoneUsesSkill.getQuickSlotIndex();
            SkillUse lastSkillUse = playerBattleState.getQuickSlotSkillUseMap().get(quickSlotIndex);
            if (lastSkillUse == null) {
                playerBattleState.getQuickSlotSkillUseMap().put(quickSlotIndex, skillUse);
            } else {
                long diff = skillUseTimestamp - lastSkillUse.getTimestamp();
                long coolingTime = game instanceof MatchplayGuardianGame ? skill.getGdCoolingTime().longValue() : skill.getCoolingTime().longValue();
                if (diff < coolingTime) {
                    playerBattleState.getQuickSlotSkillUseNoCDDetects().getAndIncrement();
                } else {
                    playerBattleState.getQuickSlotSkillUseNoCDDetects().set(0);
                }
                playerBattleState.getQuickSlotSkillUseMap().put(quickSlotIndex, skillUse);

                if (playerBattleState.getQuickSlotSkillUseNoCDDetects().get() >= 5) {
                    S2CDCMsgPacket msgPacket = new S2CDCMsgPacket(4);
                    connection.sendTCP(msgPacket);

                    GameLog gameLog = new GameLog();
                    gameLog.setGameLogType(GameLogType.BANABLE);
                    gameLog.setContent(player.getId() + " used " + skill.getName() + " before cooldown has passed. QS cooldown: " + coolingTime + ", cooldown difference: " + diff);
                    gameLog = gameLogService.save(gameLog);

                    Account account = authenticationService.findAccountById(player.getPlayer().getAccount().getId());
                    if (account != null) {
                        account.setStatus((int) AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID);
                        account.setBanReason("gameLogId: " + gameLog.getId());
                        authenticationService.updateAccount(account);
                    }

                    connection.close();

                    return false;
                }
            }
        }
        return true;
    }

    private void handleQuickSlotItemUse(FTConnection connection, FTPlayer player, CMSGPlayerUseSkill playerUseSkill) throws ValidationException {
        int slotIndex = playerUseSkill.getQuickSlotIndex();

        EquippedQuickSlots equippedQuickSlots = player.getQuickSlots();
        int itemId = switch (slotIndex) {
            case 0 -> equippedQuickSlots.slot1();
            case 1 -> equippedQuickSlots.slot2();
            case 2 -> equippedQuickSlots.slot3();
            case 3 -> equippedQuickSlots.slot4();
            case 4 -> equippedQuickSlots.slot5();
            default -> -1;
        };

        if (itemId > -1) {
            if (equippedQuickSlots.hasItem(playerUseSkill.getPlayerPocketId()) != itemId) {
                throw new ValidationException("Player tried to use a quick slot item they do not possess");
            }

            PlayerPocket playerPocket = playerPocketService.getItemAsPocket((long) itemId, player.getPocketId());
            if (playerPocket != null) {
                /*
                source value maybe not item count?
                if (playerPocket.getItemCount() != playerUseSkill.getSourceValue()) {
                    throw new ValidationException("Player tried to use a quick slot item with invalid item count");
                }
                 */

                int itemCount = playerPocket.getItemCount() - 1;

                if (itemCount <= 0) {

                    playerPocketService.remove(playerPocket.getId());
                    pocketService.decrementPocketBelongings(player.getPocketId());

                    List<Integer> quickItemSlotsList = new ArrayList<>(equippedQuickSlots.toList());
                    quickItemSlotsList.set(slotIndex, 0);

                    Player dbPlayer = player.getPlayer();
                    quickSlotEquipmentService.updateQuickSlots(dbPlayer, quickItemSlotsList);
                    player.setQuickSlots(EquippedQuickSlots.of(dbPlayer.getQuickSlotEquipment().getId(), quickItemSlotsList));

                    S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(itemId);
                    connection.sendTCP(inventoryItemRemoveAnswerPacket);
                } else {
                    playerPocket.setItemCount(itemCount);
                    playerPocketService.save(playerPocket);

                    S2CInventoryItemCountPacket inventoryItemCountPacket = new S2CInventoryItemCountPacket(playerPocket);
                    connection.sendTCP(inventoryItemCountPacket);
                }
            }
        }
    }

    private void handleSpecialSkillsUseOfGuardians(FTConnection connection, byte guardianPos, MatchplayGuardianGame game, Skill skill) {
        // There could be more special skills which need to be handled here
        if (skill.getId() == 29) { // RebirthOne
            this.handleReviveGuardian(connection, game, guardianPos, skill);
        } else if (skill.getDamage() > 1) {
            short newHealth;
            try {
                newHealth = game.getGuardianCombatSystem().heal(guardianPos, skill.getDamage().shortValue());
            } catch (ValidationException ve) {
                log.warn(ve.getMessage());
                return;
            }

            S2CMatchplayDealDamage damagePacket =
                    new S2CMatchplayDealDamage(guardianPos, newHealth, guardianPos, skill.getId().byteValue(), 0.0f, 0.0f);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(damagePacket, connection);
        }
    }

    private void handleReviveGuardian(FTConnection connection, MatchplayGuardianGame game, short guardianPos, Skill skill) {
        GuardianBattleState guardianBattleState = null;
        try {
            guardianBattleState = game.getGuardianCombatSystem().reviveAnyGuardian(skill.getDamage().shortValue());
        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
        }

        if (guardianBattleState != null) {
            S2CMatchplayDealDamage damageToPlayerPacket =
                    new S2CMatchplayDealDamage((short) guardianBattleState.getPosition(), (short) guardianBattleState.getCurrentHealth().get(), guardianPos, skill.getId().byteValue(), 0.0f, 0.0f);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(damageToPlayerPacket, connection);
        }
    }

    private void validateSkillCrystal(Queue<SkillCrystal> skillCrystals, int crystalId, int skillIndex) throws ValidationException {
        SkillCrystal skillCrystal = skillCrystals.poll();
        if (skillCrystal == null || skillCrystal.getId() != crystalId || skillCrystal.getSkillIndex() != skillIndex) {
            throw new ValidationException("Player tried to use a skill crystal they do not possess");
        }
    }
}
