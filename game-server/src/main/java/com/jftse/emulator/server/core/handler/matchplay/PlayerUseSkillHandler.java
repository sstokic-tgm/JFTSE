package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.emulator.server.core.packets.matchplay.C2SMatchplayUsesSkill;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayDealDamage;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayUseSkill;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.model.log.GameLogType;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.server.core.matchplay.battle.SkillUse;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.battle.Skill;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.QuickSlotEquipment;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.*;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.packets.S2CDCMsgPacket;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import lombok.extern.log4j.Log4j2;

@Log4j2
@PacketOperationIdentifier(PacketOperations.C2SMatchplayPlayerUseSkill)
public class PlayerUseSkillHandler extends AbstractPacketHandler {
    private C2SMatchplayUsesSkill anyoneUsesSkill;

    private final SkillService skillService;
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;
    private final AuthenticationService authenticationService;

    private final GameLogService gameLogService;

    private static final int TIMESTAMP_DELTA = 300;

    public PlayerUseSkillHandler() {
        this.skillService = ServiceManager.getInstance().getSkillService();
        this.pocketService = ServiceManager.getInstance().getPocketService();
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.quickSlotEquipmentService = ServiceManager.getInstance().getQuickSlotEquipmentService();
        this.authenticationService = ServiceManager.getInstance().getAuthenticationService();
        this.gameLogService = ServiceManager.getInstance().getGameLogService();
    }

    @Override
    public boolean process(Packet packet) {
        anyoneUsesSkill = new C2SMatchplayUsesSkill(packet);
        return true;
    }

    @Override
    public void handle() {
        handle((FTConnection) connection);
    }

    private void handle(FTConnection connection) {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getActiveGameSession() == null
                || ftClient.getPlayer() == null || ftClient.getRoomPlayer() == null)
            return;

        long skillUseTimestamp = System.currentTimeMillis() + TIMESTAMP_DELTA;
        Player player = ftClient.getPlayer();

        byte attackerPosition = anyoneUsesSkill.getAttackerPosition();
        byte targetPosition = anyoneUsesSkill.getTargetPosition();
        boolean isQuickSlot = anyoneUsesSkill.isQuickSlot();

        boolean attackerIsGuardian = attackerPosition > 9;
        boolean attackerIsPlayer = attackerPosition < 4;
        GameSession gameSession = ftClient.getActiveGameSession();
        // sometimes we are faster when cleaning up game sessions till the player is thrown back to the room
        if (gameSession == null) return;

        MatchplayGame game = gameSession.getMatchplayGame();
        if (game == null) return;

        RoomPlayer roomPlayer = ftClient.getRoomPlayer();
        Skill skill = skillService.findSkillById((long) anyoneUsesSkill.getSkillIndex() + 1);
        SkillUse skillUse = null;
        if (skill != null)
            skillUse = new SkillUse(skill, attackerPosition, targetPosition, isQuickSlot, skillUseTimestamp, false);

        if (attackerIsGuardian) {
            if (skill != null) {
                this.handleSpecialSkillsUseOfGuardians(ftClient.getConnection(), attackerPosition, (MatchplayGuardianGame) game, skill);
            }
        } else if (attackerIsPlayer) {
            if (roomPlayer != null) {
                if (isQuickSlot) {
                    if (!isQsUseValid(skillUseTimestamp, player, skill, skillUse, game, attackerPosition))
                        return;

                    this.handleQuickSlotItemUse(ftClient.getConnection(), player, anyoneUsesSkill);
                }
            }
        }

        S2CMatchplayUseSkill packet =
                new S2CMatchplayUseSkill(attackerPosition, targetPosition, anyoneUsesSkill.getSkillIndex(), anyoneUsesSkill.getSeed(), anyoneUsesSkill.getXTarget(), anyoneUsesSkill.getZTarget(), anyoneUsesSkill.getYTarget());
        gameSession.getClients().forEach(c -> {
            //if (c.getConnection().getId() != connection.getId()) {
                c.getConnection().sendTCP(packet);
            //}
        });
    }

    private boolean isQsUseValid(long skillUseTimestamp, Player player, Skill skill, SkillUse skillUse, MatchplayGame game, byte attackerPosition) {
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

                if (playerBattleState.getQuickSlotSkillUseNoCDDetects().get() >= 4) {
                    S2CDCMsgPacket msgPacket = new S2CDCMsgPacket(4);
                    connection.sendTCP(msgPacket);

                    GameLog gameLog = new GameLog();
                    gameLog.setGameLogType(GameLogType.BANABLE);
                    gameLog.setContent(player.getId() + " used " + skill.getName() + " before cooldown has passed. QS cooldown: " + coolingTime + ", cooldown difference: " + diff);
                    gameLog = gameLogService.save(gameLog);

                    Account account = authenticationService.findAccountById(player.getAccount().getId());
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

    private void handleQuickSlotItemUse(FTConnection connection, Player player, C2SMatchplayUsesSkill playerUseSkill) {
        Pocket pocket = player.getPocket();

        QuickSlotEquipment quickSlotEquipment = player.getQuickSlotEquipment();
        int itemId = switch (playerUseSkill.getQuickSlotIndex()) {
            case 0 -> quickSlotEquipment.getSlot1();
            case 1 -> quickSlotEquipment.getSlot2();
            case 2 -> quickSlotEquipment.getSlot3();
            case 3 -> quickSlotEquipment.getSlot4();
            case 4 -> quickSlotEquipment.getSlot5();
            default -> -1;
        };

        if (itemId > -1) {
            PlayerPocket playerPocket = playerPocketService.getItemAsPocket((long) itemId, pocket);
            if (playerPocket != null) {
                int itemCount = playerPocket.getItemCount() - 1;

                if (itemCount <= 0) {

                    playerPocketService.remove(playerPocket.getId());
                    pocketService.decrementPocketBelongings(pocket);

                    quickSlotEquipmentService.updateQuickSlots(quickSlotEquipment, itemId);

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

    private void handleReviveGuardian(FTConnection connection, MatchplayGuardianGame game, Skill skill) {
        GuardianBattleState guardianBattleState = null;
        try {
            guardianBattleState = game.getGuardianCombatSystem().reviveAnyGuardian(skill.getDamage().shortValue());
        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
        }

        if (guardianBattleState != null) {
            S2CMatchplayDealDamage damageToPlayerPacket =
                    new S2CMatchplayDealDamage((short) guardianBattleState.getPosition(), (short) guardianBattleState.getCurrentHealth().get(), skill.getTargeting().shortValue(), skill.getId().byteValue(), 0, 0);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(damageToPlayerPacket, connection);
        }
    }
}
