package com.jftse.emulator.server.core.handler.game.matchplay;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.matchplay.C2SMatchplayUsesSkill;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayDealDamage;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayUseSkill;
import com.jftse.emulator.server.core.service.*;
import com.jftse.entities.database.model.battle.Skill;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.QuickSlotEquipment;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PlayerUseSkillHandler extends AbstractHandler {
    private C2SMatchplayUsesSkill anyoneUsesSkill;

    private final SkillService skillService;
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;

    public PlayerUseSkillHandler() {
        this.skillService = ServiceManager.getInstance().getSkillService();
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
                || connection.getClient().getPlayer() == null || connection.getClient().getRoomPlayer() == null)
            return;

        Player player = connection.getClient().getPlayer();

        byte position = anyoneUsesSkill.getAttackerPosition();
        boolean attackerIsGuardian = position > 9;
        boolean attackerIsPlayer = position < 4;
        GameSession gameSession = connection.getClient().getActiveGameSession();
        // sometimes we are faster when cleaning up game sessions till the player is thrown back to the room
        if (gameSession == null) return;

        MatchplayGame game = gameSession.getActiveMatchplayGame();
        if (game == null) return;

        RoomPlayer roomPlayer = connection.getClient().getRoomPlayer();
        Skill skill = skillService.findSkillById((long) anyoneUsesSkill.getSkillIndex() + 1);

        if (attackerIsGuardian) {
            if (skill != null) {
                this.handleSpecialSkillsUseOfGuardians(connection, position, (MatchplayGuardianGame) game, skill);
            }
        } else if (attackerIsPlayer) {
            if (roomPlayer != null) {
                if (anyoneUsesSkill.isQuickSlot()) {
                    this.handleQuickSlotItemUse(connection, player, anyoneUsesSkill);
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

    private void handleQuickSlotItemUse(Connection connection, Player player, C2SMatchplayUsesSkill playerUseSkill) {
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
                }
            }
        }
    }

    private void handleSpecialSkillsUseOfGuardians(Connection connection, byte guardianPos, MatchplayGuardianGame game, Skill skill) {
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
                    new S2CMatchplayDealDamage((short) guardianBattleState.getPosition(), (short) guardianBattleState.getCurrentHealth().get(), (short) 0, skill.getId().byteValue(), 0, 0);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(damageToPlayerPacket, connection);
        }
    }
}
