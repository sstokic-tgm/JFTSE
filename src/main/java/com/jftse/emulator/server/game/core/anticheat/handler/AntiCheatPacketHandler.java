package com.jftse.emulator.server.game.core.anticheat.handler;

import com.jftse.emulator.server.database.model.anticheat.ClientWhitelist;
import com.jftse.emulator.server.database.model.anticheat.Module;
import com.jftse.emulator.server.game.core.packet.packets.S2CWelcomePacket;
import com.jftse.emulator.server.game.core.service.ClientWhitelistService;
import com.jftse.emulator.server.game.core.service.ModuleService;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.AntiCheatHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class AntiCheatPacketHandler {
    private final ClientWhitelistService clientWhitelistService;
    private final ModuleService moduleService;
    private final AntiCheatHandler antiCheatHandler;

    @PostConstruct
    public void init() {
    }

    public AntiCheatHandler getAntiCheatHandler() {
        return antiCheatHandler;
    }

    public void handleCleanUp() {
        List<ClientWhitelist> clientWhiteList = clientWhitelistService.findAll();
        for (int i = 0; i < clientWhiteList.size(); i++) {
            Long id = clientWhiteList.get(i).getId();
            clientWhitelistService.remove(id);
        }
    }

    public void sendWelcomePacket(Connection connection) {
        String hostAddress = connection.getRemoteAddressTCP().getAddress().getHostAddress();
        int port = connection.getRemoteAddressTCP().getPort();

        connection.getClient().setIp(hostAddress);
        connection.getClient().setPort(port);

        ClientWhitelist existingClientWhitelist = clientWhitelistService.findByIpAndHwid(hostAddress, null);
        if (existingClientWhitelist == null) {
            ClientWhitelist clientWhitelist = new ClientWhitelist();
            clientWhitelist.setIp(hostAddress);
            clientWhitelist.setPort(port);
            clientWhitelist.setFlagged(false);
            clientWhitelist.setIsAuthenticated(false);
            clientWhitelistService.save(clientWhitelist);
        }

        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(0, 0, 0, 0);
        connection.sendTCP(welcomePacket);
    }

    public void handleDisconnected(Connection connection) {
        String hostAddress = connection.getClient().getIp();
        ClientWhitelist clientWhitelist = clientWhitelistService.findByIpAndHwid(hostAddress, connection.getHwid());
        if (clientWhitelist != null)
            clientWhitelistService.remove(clientWhitelist.getId());

        connection.setClient(null);
        connection.close();
    }

    public void handleRegister(Connection connection, Packet packet) {
        String hostAddress = connection.getRemoteAddressTCP().getAddress().getHostAddress();

        ClientWhitelist clientWhitelist = clientWhitelistService.findByIpAndHwid(hostAddress, null);
        if (clientWhitelist != null && !clientWhitelist.getIsAuthenticated()) {
            String hwid = packet.readString();
            connection.setHwid(hwid);

            clientWhitelist.setHwid(hwid);
            clientWhitelist.setIsAuthenticated(true);
            clientWhitelistService.save(clientWhitelist);
        }
    }

    public void handleUnknown(Connection connection, Packet packet) {
        Packet unknownAnswer = new Packet((char) (packet.getPacketId() + 1));
        switch (unknownAnswer.getPacketId()) {

            case 0x9795:
                String moduleName = packet.readUnicodeString();
                Module module = moduleService.findModuleByName(moduleName);
                if (module == null) {
                    module = new Module();
                    module.setName(moduleName);
                    module.setBlock(false);
                    module = moduleService.save(module);

                    unknownAnswer.write(0);
                }
                else {
                    if (module.getBlock())
                        unknownAnswer.write(1);
                    else
                        unknownAnswer.write(0);
                }
                break;

            default:
                unknownAnswer.write((short) 0);
                break;
        }
        connection.sendTCP(unknownAnswer);
    }
}