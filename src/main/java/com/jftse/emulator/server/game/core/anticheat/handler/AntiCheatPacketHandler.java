package com.jftse.emulator.server.game.core.anticheat.handler;

import com.jftse.emulator.server.database.model.anticheat.ClientWhitelist;
import com.jftse.emulator.server.database.model.anticheat.Module;
import com.jftse.emulator.server.game.core.packet.packets.S2CWelcomePacket;
import com.jftse.emulator.server.game.core.service.ClientWhitelistService;
import com.jftse.emulator.server.game.core.service.ModuleService;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.AntiCheatHandler;
import com.jftse.emulator.server.shared.module.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        // empty...
    }

    public void sendWelcomePacket(Connection connection) {
        if (connection.getRemoteAddressTCP() != null) {
            String hostAddress = connection.getRemoteAddressTCP().getAddress().getHostAddress();
            int port = connection.getRemoteAddressTCP().getPort();

            connection.getClient().setIp(hostAddress);
            connection.getClient().setPort(port);

            ClientWhitelist clientWhitelist = new ClientWhitelist();
            clientWhitelist.setIp(hostAddress);
            clientWhitelist.setPort(port);
            clientWhitelist.setFlagged(false);
            clientWhitelist.setIsAuthenticated(false);
            clientWhitelist.setIsActive(true);
            clientWhitelistService.save(clientWhitelist);

            S2CWelcomePacket welcomePacket = new S2CWelcomePacket(0, 0, 0, 0);
            connection.sendTCP(welcomePacket);
        }
    }

    public void handleDisconnected(Connection connection) {
        if (connection.getClient() != null) {
            String hostAddress = connection.getClient().getIp();
            ClientWhitelist clientWhitelist = clientWhitelistService.findByIpAndHwid(hostAddress, connection.getHwid());
            if (clientWhitelist != null) {
                clientWhitelist.setIsActive(false);
                clientWhitelistService.save(clientWhitelist);
            }

            this.antiCheatHandler.removeClient(connection.getClient());
            connection.setClient(null);
        }
        connection.close();
    }

    public void handleRegister(Connection connection, Packet packet) {
        if (connection.getRemoteAddressTCP() != null) {
            String hostAddress = connection.getRemoteAddressTCP().getAddress().getHostAddress();

            if (connection.getClient() != null) {
                Client client = connection.getClient();
                Map<String, Boolean> files = this.antiCheatHandler.getFilesByClient(client);

                String str = packet.readString();
                List<String> result = Arrays.asList(str.split(";"));

                if (result.size() == 3) {
                    files.entrySet().stream()
                            .filter(f -> result.containsAll(Arrays.asList(f.getKey().split(";"))))
                            .findFirst()
                            .ifPresent(f -> f.setValue(true));
                    boolean valid = files.entrySet().stream().allMatch(Map.Entry::getValue);

                    if (valid) {
                        Packet unknownAnswer = new Packet((char) 0x9791);
                        unknownAnswer.write((byte) 0);
                        connection.sendTCP(unknownAnswer);
                    }
                } else {
                    boolean valid = files.entrySet().stream().allMatch(Map.Entry::getValue);
                    if (valid) {
                        ClientWhitelist clientWhitelist = clientWhitelistService.findByIpAndHwid(hostAddress, null);
                        if (clientWhitelist != null && !clientWhitelist.getIsAuthenticated()) {
                            String hwid = result.get(0);
                            connection.setHwid(hwid);

                            clientWhitelist.setHwid(hwid);
                            clientWhitelist.setIsAuthenticated(true);
                            clientWhitelistService.save(clientWhitelist);

                            Packet unknownAnswer = new Packet((char) 0x9791);
                            unknownAnswer.write((byte) 0);
                            connection.sendTCP(unknownAnswer);
                        }
                    } else {
                        Packet unknownAnswer = new Packet((char) 0x9791);
                        unknownAnswer.write((byte) 2);
                        connection.sendTCP(unknownAnswer);
                    }
                }
            }
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