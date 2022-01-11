package com.jftse.emulator.server.core.handler.game.matchplay;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.authserver.S2CLoginAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.matchplay.C2SMatchplayUsesSkill;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayDealDamage;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayUseSkill;
import com.jftse.emulator.server.core.service.*;
import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.database.model.battle.Skill;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.QuickSlotEquipment;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.database.model.pocket.Pocket;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.extern.log4j.Log4j2;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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
        RoomPlayer roomPlayer = roomPlayers.stream()
                .filter(x -> x.getPlayer() != null && x.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                .findAny()
                .orElse(null);
        Skill skill = skillService.findSkillById((long) anyoneUsesSkill.getSkillIndex() + 1);

        if (attackerIsGuardian) {
            if (skill != null) {
                this.handleSpecialSkillsUseOfGuardians(connection, position, game, roomPlayers, skill);
            }
        } else if (attackerIsPlayer) {
            if (roomPlayer != null) {
                PlayerBattleState playerBattleState = game.getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition() == roomPlayer.getPosition())
                        .findFirst()
                        .orElse(null);

                if (anyoneUsesSkill.isQuickSlot()) {
                    if (playerBattleState != null) {
                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

                        if (playerBattleState.getLastQS().containsKey(skill.getId()) && playerBattleState.getLastQSCounter().containsKey(skill.getId())) {
                            long lastQSUseTime = playerBattleState.getLastQS().get(skill.getId());
                            int counter = playerBattleState.getLastQSCounter().get(skill.getId());

                            long latency = connection.getLatency();
                            lastQSUseTime -= (latency + 4950);
                            long timePassed = cal.getTimeInMillis() - lastQSUseTime;

                            if (timePassed >= skill.getGdCoolingTime().longValue()) {
                                this.handleQuickSlotItemUse(connection, anyoneUsesSkill);
                                playerBattleState.getLastQS().put(skill.getId(), cal.getTimeInMillis());
                                playerBattleState.getLastQSCounter().put(skill.getId(), 0);
                            } else {
                                if (counter > 5) {
                                    log.info("[Guardian] No QS CD detection\nlatency: " + latency + "\ntimePassed: " + timePassed + "\nlastQSUseTime: " + lastQSUseTime + "\nskill: " + skill.getName() + "\nskill-CD: " + skill.getGdCoolingTime() + "\nplayerName: " + roomPlayer.getPlayer().getName());
                                    playerBattleState.setCurrentHealth((short) 0);
                                    playerBattleState.setDead(true);
                                    S2CMatchplayDealDamage matchplayDealDamage = new S2CMatchplayDealDamage((short) position, (short) playerBattleState.getCurrentHealth(), skill.getTargeting().shortValue(), (byte) 3, 0, 0);
                                    S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", roomPlayer.getPlayer().getName() + " died because of no QS CD hack. Marked for ban.");
                                    GameManager.getInstance().sendPacketToAllClientsInSameGameSession(matchplayDealDamage, connection);
                                    GameManager.getInstance().sendPacketToAllClientsInSameGameSession(chatRoomAnswerPacket, connection);

                                    Account account = authenticationService.findAccountById(connection.getClient().getAccount().getId());
                                    account.setStatus((int) S2CLoginAnswerPacket.ACCOUNT_BLOCKED_USER_ID);
                                    account.setBanReason("No QS CD hack in guardian mode.");
                                    this.authenticationService.updateAccount(account);
                                    return;
                                } else {
                                    counter++;
                                    playerBattleState.getLastQSCounter().put(skill.getId(), counter);
                                }
                            }

                        } else {
                            this.handleQuickSlotItemUse(connection, anyoneUsesSkill);
                            playerBattleState.getLastQS().put(skill.getId(), cal.getTimeInMillis());
                            playerBattleState.getLastQSCounter().put(skill.getId(), 0);
                        }
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

    private void handleSpecialSkillsUseOfGuardians(Connection connection, byte guardianPos, MatchplayGuardianGame game, List<RoomPlayer> roomPlayers, Skill skill) {
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
        } /*else if (skill.getId() == 9) { // Miniam needs to be treated individually
            int roomPlayersSize = roomPlayers.size();
            for (int i = 0; i < roomPlayersSize; i++) {
                RoomPlayer roomPlayer = roomPlayers.poll();

                Short newHealth;
                try {
                    newHealth = game.getGuardianCombatSystem().dealDamageToPlayer(guardianPos, roomPlayer.getPosition(), skill.getDamage().shortValue(), false, false);
                } catch (ValidationException ve) {
                    roomPlayers.offer(roomPlayer);
                    log.warn(ve.getMessage());
                    continue;
                }

                S2CMatchplayDealDamage damagePacket =
                        new S2CMatchplayDealDamage(roomPlayer.getPosition(), newHealth, skill.getTargeting().shortValue(), skill.getId().byteValue(), 0, 0);
                GameManager.getInstance().sendPacketToAllClientsInSameGameSession(damagePacket, connection);

                roomPlayers.offer(roomPlayer);
            }
        }*/
    }

    private void handleReviveGuardian(Connection connection, MatchplayGuardianGame game, Skill skill) {
        GuardianBattleState guardianBattleState = null;
        try {
            guardianBattleState = game.getGuardianCombatSystem().reviveAnyGuardian(skill.getDamage().shortValue());
        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
            return;
        }
        if (guardianBattleState != null) {
            S2CMatchplayDealDamage damageToPlayerPacket =
                    new S2CMatchplayDealDamage((short) guardianBattleState.getPosition(), (short) guardianBattleState.getCurrentHealth(), (short) 0, skill.getId().byteValue(), 0, 0);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(damageToPlayerPacket, connection);
        }
    }
}
