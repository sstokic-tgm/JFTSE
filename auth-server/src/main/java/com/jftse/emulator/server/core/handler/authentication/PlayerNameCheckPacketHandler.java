package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.ProfaneWordsService;
import com.jftse.server.core.shared.packets.auth.CMSGPlayerNameCheck;
import com.jftse.server.core.shared.packets.auth.SMSGPlayerNameCheck;

@PacketId(CMSGPlayerNameCheck.PACKET_ID)
public class PlayerNameCheckPacketHandler implements PacketHandler<FTConnection, CMSGPlayerNameCheck> {
    private final ProfaneWordsService profaneWordsService;
    private final PlayerService playerService;

    public PlayerNameCheckPacketHandler() {
        profaneWordsService = ServiceManager.getInstance().getProfaneWordsService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public void handle(FTConnection connection, CMSGPlayerNameCheck playerNameCheckPacket) {
        String playerName = playerNameCheckPacket.getName();
        boolean isPlayerNameValid = !profaneWordsService.textContainsProfaneWord(playerName);

        Player player = playerService.findByName(playerName);
        if (player == null && isPlayerNameValid) {
            SMSGPlayerNameCheck response = SMSGPlayerNameCheck.builder().result((char) 0).build();
            connection.sendTCP(response);
        } else {
            SMSGPlayerNameCheck response = SMSGPlayerNameCheck.builder().result((char) -1).build();
            connection.sendTCP(response);
        }
    }
}
