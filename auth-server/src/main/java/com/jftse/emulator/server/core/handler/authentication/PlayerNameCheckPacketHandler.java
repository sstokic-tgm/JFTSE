package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.player.C2SPlayerNameCheckPacket;
import com.jftse.emulator.server.core.packets.player.S2CPlayerNameCheckAnswerPacket;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.ProfaneWordsService;

@PacketOperationIdentifier(PacketOperations.C2SPlayerNameCheck)
public class PlayerNameCheckPacketHandler extends AbstractPacketHandler {
    private C2SPlayerNameCheckPacket playerNameCheckPacket;

    private final ProfaneWordsService profaneWordsService;
    private final PlayerService playerService;

    public PlayerNameCheckPacketHandler() {
        profaneWordsService = ServiceManager.getInstance().getProfaneWordsService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        playerNameCheckPacket = new C2SPlayerNameCheckPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        String playerName = playerNameCheckPacket.getNickname();
        boolean isPlayerNameValid = !profaneWordsService.textContainsProfaneWord(playerName);

        Player player = playerService.findByName(playerName);
        if (player == null && isPlayerNameValid) {
            S2CPlayerNameCheckAnswerPacket playerNameCheckAnswerPacket = new S2CPlayerNameCheckAnswerPacket((char) 0);
            connection.sendTCP(playerNameCheckAnswerPacket);
        } else {
            S2CPlayerNameCheckAnswerPacket playerNameCheckAnswerPacket = new S2CPlayerNameCheckAnswerPacket((char) -1);
            connection.sendTCP(playerNameCheckAnswerPacket);
        }
    }
}
