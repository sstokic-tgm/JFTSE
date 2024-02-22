package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.authserver.S2CGameServerListPacket;
import com.jftse.emulator.server.core.packets.player.S2CPlayerListPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.AuthenticationService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PlayerService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SLoginAliveClient)
public class LoginAliveClientHandler extends AbstractPacketHandler {
    private final AuthenticationService authenticationService;
    private final PlayerService playerService;
    private final PlayerPocketService playerPocketService;

    public LoginAliveClientHandler() {
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
        playerService = ServiceManager.getInstance().getPlayerService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null)
            return;

        Account account = client.getAccount();
        if (account == null)
            return;

        if (client.isClientAlive().compareAndSet(false, true)) {
            int tutorialCount = playerService.getTutorialProgressSucceededCountByAccount(account.getId());
            List<Player> playerList = playerService.findAllByAccount(account);

            for (Player p : playerList) {
                List<PlayerPocket> ppList = playerPocketService.getPlayerPocketItems(p.getPocket());
                final boolean nameChangeItemPresent = ppList.stream()
                        .anyMatch(pp -> pp.getCategory().equals(EItemCategory.SPECIAL.getName()) && pp.getItemIndex() == 4);
                if (nameChangeItemPresent && !p.getNameChangeAllowed()) {
                    p.setNameChangeAllowed(true);
                    p = playerService.save(p);
                }
            }

            S2CPlayerListPacket playerListPacket = new S2CPlayerListPacket(account, playerList, tutorialCount);
            connection.sendTCP(playerListPacket);

            S2CGameServerListPacket gameServerListPacket = new S2CGameServerListPacket(authenticationService.getGameServerList());
            connection.sendTCP(gameServerListPacket);
        }
    }
}
