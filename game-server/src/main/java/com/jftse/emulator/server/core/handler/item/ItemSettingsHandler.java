package com.jftse.emulator.server.core.handler.item;

import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.packets.item.C2SResponseItemSettingsPacket;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayBackToRoom;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.battle.Skill;
import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.model.log.GameLogType;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.AuthenticationService;
import com.jftse.server.core.service.GameLogService;
import com.jftse.server.core.service.SkillService;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.packets.S2CDCMsgPacket;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

@Log4j2
@PacketOperationIdentifier(PacketOperations.C2SResponseItemSettings)
public class ItemSettingsHandler extends AbstractPacketHandler {
    private C2SResponseItemSettingsPacket itemSettingsPacket;

    private final AuthenticationService authenticationService;
    private final SkillService skillService;
    private final GameLogService gameLogService;

    public ItemSettingsHandler() {
        this.authenticationService = ServiceManager.getInstance().getAuthenticationService();
        this.skillService = ServiceManager.getInstance().getSkillService();
        this.gameLogService = ServiceManager.getInstance().getGameLogService();
    }

    @Override
    public boolean process(Packet packet) {
        itemSettingsPacket = new C2SResponseItemSettingsPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        GameSession gameSession = ftClient.getActiveGameSession();
        if (gameSession == null)
            return;

        Integer gameSessionId = ftClient.getGameSessionId();

        Player player = ftClient.getPlayer();

        final Map<Skill, C2SResponseItemSettingsPacket.ItemSettings> mapOfNonMatchingSkills = new HashMap<>(itemSettingsPacket.getSize());
        final List<C2SResponseItemSettingsPacket.ItemSettings> itemSettings = itemSettingsPacket.getItemSettings();
        for (C2SResponseItemSettingsPacket.ItemSettings itemSetting : itemSettings) {
            Skill skill = this.skillService.findSkillById(itemSetting.getId());
            if (skill == null)
                continue;

            byte damage = skill.getDamage().byteValue();
            byte damageRate = skill.getDamageRate().byteValue();
            byte coolingTime = (byte) ((int) (skill.getCoolingTime() / 100.0));
            byte gdCoolingTime = (byte) ((int) (skill.getGdCoolingTime() / 100.0));
            byte shotCnt = skill.getShotCnt().byteValue();

            if (damage != itemSetting.getDamage() || damageRate != itemSetting.getDamageRate() || coolingTime != itemSetting.getCoolingTime() || gdCoolingTime != itemSetting.getGdCoolingTime() || shotCnt != itemSetting.getShotCnt()) {
                mapOfNonMatchingSkills.put(skill, itemSetting);
            }
        }

        if (mapOfNonMatchingSkills.isEmpty())
            return;

        StringBuilder sb = new StringBuilder();
        sb.append("Player: ").append(player.getName()).append(" has modified the following skills: ");
        for (Map.Entry<Skill, C2SResponseItemSettingsPacket.ItemSettings> entry : mapOfNonMatchingSkills.entrySet()) {
            Skill skill = entry.getKey();
            C2SResponseItemSettingsPacket.ItemSettings itemSetting = entry.getValue();
            sb.append(skill.getName())
                    .append(" (");
            sb.append("Damage: ").append(skill.getDamage().byteValue()).append(" -> ").append(itemSetting.getDamage()).append(", ");
            sb.append("Damage Rate: ").append(skill.getDamageRate().byteValue()).append(" -> ").append(itemSetting.getDamageRate()).append(", ");
            sb.append("Cooling Time: ").append((byte) ((int) (skill.getCoolingTime() / 100.0))).append(" -> ").append(itemSetting.getCoolingTime()).append(", ");
            sb.append("GD Cooling Time: ").append((byte) ((int) (skill.getGdCoolingTime() / 100.0))).append(" -> ").append(itemSetting.getGdCoolingTime()).append(", ");
            sb.append("Shot Count: ").append(skill.getShotCnt().byteValue()).append(" -> ").append(itemSetting.getShotCnt())
                    .append(")");
        }

        GameLog gameLog = new GameLog();
        gameLog.setGameLogType(GameLogType.BANABLE);
        gameLog.setContent(sb.toString());
        gameLog = this.gameLogService.save(gameLog);

        S2CDCMsgPacket msgPacket = new S2CDCMsgPacket(4);
        connection.sendTCP(msgPacket);

        Account account = authenticationService.findAccountById(player.getAccount().getId());
        if (account != null) {
            account.setStatus((int) AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID);
            account.setBanReason("gameLogId: " + gameLog.getId());
            authenticationService.updateAccount(account);
        }

        connection.close();

        ConcurrentLinkedDeque<FTClient> clients = gameSession.getClients();

        for (FTClient client : clients) {
            RoomPlayer rp = client.getRoomPlayer();
            if (rp == null)
                continue;

            S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
            client.getConnection().sendTCP(backToRoomPacket);

            client.setActiveGameSession(null);
        }

        gameSession.getClients().removeIf(c -> c.getActiveGameSession() == null);
        GameSessionManager.getInstance().removeGameSession(gameSessionId, gameSession);
    }
}
