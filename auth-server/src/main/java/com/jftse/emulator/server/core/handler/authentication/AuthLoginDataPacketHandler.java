package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.authserver.C2SAuthLoginPacket;
import com.jftse.emulator.server.core.packets.authserver.S2CAuthLoginPacket;
import com.jftse.emulator.server.core.packets.authserver.S2CLoginAnswerPacket;
import com.jftse.emulator.server.core.packets.player.S2CPlayerListPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.auth.AuthToken;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.AuthTokenService;
import com.jftse.server.core.service.AuthenticationService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;

@PacketOperationIdentifier(PacketOperations.C2SAuthLoginData)
@Log4j2
public class AuthLoginDataPacketHandler extends AbstractPacketHandler {
    private C2SAuthLoginPacket authLoginPacket;

    private final AuthenticationService authenticationService;
    private final AuthTokenService authTokenService;
    private final PlayerService playerService;

    public AuthLoginDataPacketHandler() {
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
        authTokenService = ServiceManager.getInstance().getAuthTokenService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        authLoginPacket = new C2SAuthLoginPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Account account = authenticationService.findAccountByUsername(authLoginPacket.getUsername());
        if (account != null && account.getStatus().shortValue() != AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID) {
            account.setStatus((int) AuthenticationServiceImpl.ACCOUNT_ALREADY_LOGGED_IN);
            account = authenticationService.updateAccount(account);
            FTClient ftClient = connection.getClient();
            ftClient.setAccount(account.getId());

            log.info(account.getUsername() + " connected");

            String token = StringUtils.randomString(16);
            long timestamp = Instant.now().toEpochMilli();

            AuthToken authToken = new AuthToken();
            authToken.setToken(token);
            authToken.setLoginTimestamp(timestamp);
            authToken.setAccountName(authLoginPacket.getUsername());
            authTokenService.save(authToken);

            S2CAuthLoginPacket authLoginAnswerPacket = new S2CAuthLoginPacket((char) 0, (byte) 1);
            connection.sendTCP(authLoginAnswerPacket);

            S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(AuthenticationServiceImpl.SUCCESS, token, timestamp);
            connection.sendTCP(loginAnswerPacket);

            S2CPlayerListPacket PlayerListPacket = new S2CPlayerListPacket(account, playerService.findAllByAccount(account));
            connection.sendTCP(PlayerListPacket);
        } else {
            S2CAuthLoginPacket authLoginAnswerPacket = new S2CAuthLoginPacket((char) -1, (byte) 0);
            connection.sendTCP(authLoginAnswerPacket);
            connection.close();
        }
    }
}
