package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.player.S2CPlayerListPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PlayerService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SRequestPlayerList)
public class PlayerListRequestHandler extends AbstractPacketHandler {
    private final PlayerService playerService;

    public PlayerListRequestHandler() {
        this.playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null) {
            return;
        }

        Account account = client.getAccount();
        if (account == null) {
            return;
        }

        int tutorialCount = playerService.getTutorialProgressSucceededCountByAccount(account.getId());
        List<Player> playerList = playerService.findAllByAccount(account);

        S2CPlayerListPacket playerListPacket = new S2CPlayerListPacket(account, playerList, tutorialCount);
        connection.sendTCP(playerListPacket);
    }
}
