package com.jftse.emulator.server.game.core.anticheat.handler;

import com.jftse.emulator.server.database.model.anticheat.ClientWhitelist;
import com.jftse.emulator.server.game.core.packet.packets.S2CWelcomePacket;
import com.jftse.emulator.server.game.core.service.ClientWhitelistService;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.AntiCheatHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Log4j2
public class AntiCheatPacketHandler {
    private final ClientWhitelistService clientWhitelistService;
    private final AntiCheatHandler antiCheatHandler;

    @PostConstruct
    public void init() {
    }

    public AntiCheatHandler getAntiCheatHandler() {
        return antiCheatHandler;
    }

    public void handleCleanUp() {
    }

    public void sendWelcomePacket(Connection connection) {
        String hostAddress = connection.getRemoteAddressTCP().getAddress().getHostAddress();
        int port = connection.getRemoteAddressTCP().getPort();

        connection.getClient().setIp(hostAddress);
        connection.getClient().setPort(port);

        // dunno why we get duplicate entries so check and only whitelist a client if no entry exists, if this handles multiple clients, stays open..
        ClientWhitelist existingClientWhitelist = clientWhitelistService.findByIp(hostAddress);
        if (existingClientWhitelist == null) {
            ClientWhitelist clientWhitelist = new ClientWhitelist();
            clientWhitelist.setIp(hostAddress);
            clientWhitelist.setPort(port);
            clientWhitelist.setFlagged(false);
            clientWhitelistService.save(clientWhitelist);
        }

        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(0, 0, 0, 0);
        connection.sendTCP(welcomePacket);
    }

    public void handleDisconnected(Connection connection) {
        String hostAddress = connection.getClient().getIp();
        ClientWhitelist clientWhitelist = clientWhitelistService.findByIp(hostAddress);
        if (clientWhitelist != null)
            clientWhitelistService.remove(clientWhitelist.getId());

        connection.setClient(null);
        connection.close();
    }

    public void handleUnknown(Connection connection, Packet packet) {
        Packet unknownAnswer = new Packet((char) (packet.getPacketId() + 1));
        unknownAnswer.write((short) 0);
        connection.sendTCP(unknownAnswer);
    }
}