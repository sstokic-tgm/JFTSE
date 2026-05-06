package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.common.utilities.ResourceUtil;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.anticheat.ClientWhitelist;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.AuthenticationService;
import com.jftse.server.core.service.ClientWhitelistService;
import com.jftse.server.core.shared.packets.ac.CMSGAntiCheatSetFTGlobalVars;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.Properties;

@Log4j2
@PacketId(CMSGAntiCheatSetFTGlobalVars.PACKET_ID)
public class FTGlobalVarSetHandler implements PacketHandler<FTConnection, CMSGAntiCheatSetFTGlobalVars> {
    private final ClientWhitelistService clientWhitelistService;
    private final AuthenticationService authenticationService;

    public FTGlobalVarSetHandler() {
        clientWhitelistService = ServiceManager.getInstance().getClientWhitelistService();
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
    }

    @Override
    public void handle(FTConnection connection, CMSGAntiCheatSetFTGlobalVars packet) {
        FTClient client = connection.getClient();
        if (client == null)
            return;

        Optional<Path> p = ResourceUtil.getPath(FTGlobalVarHandler.FTGLOBALVARS_PROPERTIES);
        if (p.isEmpty())
            return;

        Properties properties = new Properties();
        try (InputStream is = Files.newInputStream(p.get(), StandardOpenOption.READ)) {
            properties.load(is);

            properties.setProperty(packet.getConfName(), String.valueOf(packet.getConfValue()));
            String lastAccounts = properties.getProperty("lastEditByAccounts");

            ClientWhitelist clientWhitelist = clientWhitelistService.findByIpAndHwid(client.getIp(), connection.getHwid());
            if (clientWhitelist != null && clientWhitelist.getAccount() != null) {
                Account account = authenticationService.findAccountById(clientWhitelist.getAccount().getId());
                if (account != null) {
                    properties.setProperty("lastEditByAccounts", lastAccounts + "," + account.getUsername());
                }
            }

        } catch (IOException e) {
            log.error("Error reading the properties file: " + e.getMessage(), e);
        }

        try (OutputStream os = Files.newOutputStream(p.get(), StandardOpenOption.WRITE)) {
            properties.store(os, null);
        } catch (IOException e) {
            log.error("Error writing the properties file: " + e.getMessage(), e);
        }
    }
}
