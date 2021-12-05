package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.service.AuthTokenService;
import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.database.model.anticheat.ClientWhitelist;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.S2CDisconnectAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.authserver.C2SLoginPacket;
import com.jftse.emulator.server.core.packet.packets.authserver.S2CGameServerListPacket;
import com.jftse.emulator.server.core.packet.packets.authserver.S2CLoginAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.player.S2CPlayerListPacket;
import com.jftse.emulator.server.core.service.AuthenticationService;
import com.jftse.emulator.server.core.service.ClientWhitelistService;
import com.jftse.emulator.server.database.model.auth.AuthToken;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Log4j2
public class LoginPacketHandler extends AbstractHandler {
    private C2SLoginPacket loginPacket;

    private final AuthenticationService authenticationService;
    private final ClientWhitelistService clientWhitelistService;
    private final AuthTokenService authTokenService;

    private final ConfigService configService;

    public LoginPacketHandler() {
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
        clientWhitelistService = ServiceManager.getInstance().getClientWhitelistService();
        authTokenService = ServiceManager.getInstance().getAuthTokenService();

        configService = ServiceManager.getInstance().getConfigService();
    }

    @Override
    public boolean process(Packet packet) {
        loginPacket = new C2SLoginPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (configService.getValue("anticheat.enabled", false) && !isClientValid(connection.getRemoteAddressTCP(), loginPacket.getHwid())) {
            S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(S2CLoginAnswerPacket.INVAILD_VERSION);
            connection.sendTCP(loginAnswerPacket);

            S2CDisconnectAnswerPacket disconnectAnswerPacket = new S2CDisconnectAnswerPacket();
            connection.sendTCP(disconnectAnswerPacket);
            return;
        }

        // version check
        if (loginPacket.getVersion() != 21108180) {
            S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(S2CLoginAnswerPacket.INVAILD_VERSION);
            connection.sendTCP(loginAnswerPacket);

            S2CDisconnectAnswerPacket disconnectAnswerPacket = new S2CDisconnectAnswerPacket();
            connection.sendTCP(disconnectAnswerPacket);
            return;
        }

        Account account = authenticationService.login(loginPacket.getUsername(), loginPacket.getPassword());

        if (account == null) {
            S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(S2CLoginAnswerPacket.ACCOUNT_INVALID_USER_ID);
            connection.sendTCP(loginAnswerPacket);
        } else {
            Integer accountStatus = account.getStatus();
            if (accountStatus.equals((int) S2CLoginAnswerPacket.ACCOUNT_BLOCKED_USER_ID)
                    && account.getBannedUntil() != null && account.getBannedUntil().getTime() < new Date().getTime()) {
                account.setStatus(0);
                account.setBannedUntil(null);
                account.setBanReason(null);
                accountStatus = 0;
            }

            if (!accountStatus.equals((int) S2CLoginAnswerPacket.SUCCESS) || isClientFlagged(connection.getRemoteAddressTCP(), loginPacket.getHwid())) {
                S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(accountStatus.shortValue());
                connection.sendTCP(loginAnswerPacket);
            } else {
                if (configService.getValue("anticheat.enabled", false) && !linkAccountToClientWhitelist(connection.getRemoteAddressTCP(), loginPacket.getHwid(), account)) {
                    S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket((short) -80);
                    connection.sendTCP(loginAnswerPacket);
                    return;
                }

                // set last login date
                account.setLastLogin(new Date());
                // mark as logged in
                account.setStatus((int) S2CLoginAnswerPacket.ACCOUNT_ALREADY_LOGGED_IN);
                account = authenticationService.updateAccount(account);

                connection.getClient().setAccount(account);

                AuthToken existingAuthToken = authTokenService.findAuthTokenByAccountName(account.getUsername());
                if (existingAuthToken != null) {
                    authTokenService.remove(existingAuthToken);
                }

                String token = StringUtils.randomString(16);
                long timestamp = Instant.now().toEpochMilli();

                AuthToken authToken = new AuthToken();
                authToken.setToken(token);
                authToken.setLoginTimestamp(timestamp);
                authToken.setAccountName(account.getUsername());
                authTokenService.save(authToken);

                S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(S2CLoginAnswerPacket.SUCCESS, token, timestamp);
                connection.sendTCP(loginAnswerPacket);

                S2CPlayerListPacket PlayerListPacket = new S2CPlayerListPacket(account, account.getPlayerList());
                connection.sendTCP(PlayerListPacket);

                S2CGameServerListPacket gameServerListPacket = new S2CGameServerListPacket(authenticationService.getGameServerList());
                connection.sendTCP(gameServerListPacket);

                String hostAddress;
                if (connection.getRemoteAddressTCP() != null)
                    hostAddress = connection.getRemoteAddressTCP().getAddress().getHostAddress();
                else
                    hostAddress = "null";
                log.info(account.getUsername() + " has logged in from " + hostAddress + " with hwid " + loginPacket.getHwid());
            }
        }
    }

    private boolean isClientValid(InetSocketAddress inetSocketAddress, String hwid) {
        if (inetSocketAddress == null)
            return false;
        String hostAddress = inetSocketAddress.getAddress().getHostAddress();
        ClientWhitelist clientWhitelist = clientWhitelistService.findByIpAndHwid(hostAddress, hwid);
        return clientWhitelist != null;
    }

    private boolean isClientFlagged(InetSocketAddress inetSocketAddress, String hwid) {
        if (inetSocketAddress == null)
            return false;
        String hostAddress = inetSocketAddress.getAddress().getHostAddress();
        ClientWhitelist clientWhitelist = clientWhitelistService.findByIpAndHwidAndFlaggedTrue(hostAddress, hwid);
        return clientWhitelist != null && clientWhitelist.getFlagged();
    }

    private boolean linkAccountToClientWhitelist(InetSocketAddress inetSocketAddress, String hwid, Account account) {
        if (inetSocketAddress != null) {
            String hostAddress = inetSocketAddress.getAddress().getHostAddress();
            ClientWhitelist clientWhitelist = clientWhitelistService.findByIpAndHwid(hostAddress, hwid);
            if (clientWhitelist != null) {
                clientWhitelist.setAccount(account);
                clientWhitelistService.save(clientWhitelist);
                return true;
            } else {
                return false;
            }
        } else
            return false;
    }
}
