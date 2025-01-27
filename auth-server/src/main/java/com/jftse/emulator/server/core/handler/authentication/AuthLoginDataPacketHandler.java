package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.authserver.C2SAuthLoginPacket;
import com.jftse.emulator.server.core.packets.authserver.S2CAuthLoginPacket;
import com.jftse.emulator.server.core.packets.authserver.S2CGameServerListPacket;
import com.jftse.emulator.server.core.packets.authserver.S2CLoginAnswerPacket;
import com.jftse.emulator.server.core.packets.player.S2CPlayerListPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.auth.AuthToken;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.AuthTokenService;
import com.jftse.server.core.service.AuthenticationService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SAuthLoginData)
@Log4j2
public class AuthLoginDataPacketHandler extends AbstractPacketHandler {
    private C2SAuthLoginPacket authLoginPacket;

    private final AuthenticationService authenticationService;
    private final AuthTokenService authTokenService;
    private final PlayerService playerService;
    private final PlayerPocketService playerPocketService;

    public AuthLoginDataPacketHandler() {
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
        authTokenService = ServiceManager.getInstance().getAuthTokenService();
        playerService = ServiceManager.getInstance().getPlayerService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
    }

    @Override
    public boolean process(Packet packet) {
        authLoginPacket = new C2SAuthLoginPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Account account = authenticationService.findAccountByUsername(authLoginPacket.getUsername());
        FTClient client = (FTClient) connection.getClient();
        if (client != null && account != null && account.getStatus().shortValue() != AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID && client.isLoginIn().compareAndSet(false, true)) {
            S2CAuthLoginPacket authLoginAnswerPacket = new S2CAuthLoginPacket((char) 0);
            connection.sendTCP(authLoginAnswerPacket);

            if (account.getStatus() == AuthenticationServiceImpl.ACCOUNT_ALREADY_LOGGED_IN) {
                account.setStatus((int) AuthenticationServiceImpl.SUCCESS);
            }

            account.setLoggedInServer(ServerType.AUTH_SERVER);
            account = authenticationService.updateAccount(account);

            client.setAccount(account.getId());
            log.info(account.getUsername() + " connected");

            String token = StringUtils.randomString(16);
            long timestamp = Instant.now().toEpochMilli();

            AuthToken authToken = new AuthToken();
            authToken.setToken(token);
            authToken.setLoginTimestamp(timestamp);
            authToken.setAccountName(authLoginPacket.getUsername());
            authTokenService.save(authToken);

            S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(AuthenticationServiceImpl.SUCCESS, token, timestamp);
            connection.sendTCP(loginAnswerPacket);

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

            client.isLoginIn().set(false);
        } else {
            S2CAuthLoginPacket authLoginAnswerPacket = new S2CAuthLoginPacket((char) -1);
            connection.sendTCP(authLoginAnswerPacket);
            connection.close();
        }
    }
}
