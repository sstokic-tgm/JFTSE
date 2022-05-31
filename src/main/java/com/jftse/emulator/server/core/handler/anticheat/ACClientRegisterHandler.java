package com.jftse.emulator.server.core.handler.anticheat;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.AntiCheatManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.service.ClientWhitelistService;
import com.jftse.emulator.server.database.model.anticheat.ClientWhitelist;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ACClientRegisterHandler extends AbstractHandler {
    private Packet packet;

    private String str;

    private final ClientWhitelistService clientWhitelistService;

    public  ACClientRegisterHandler() {
        clientWhitelistService = ServiceManager.getInstance().getClientWhitelistService();
    }

    @Override
    public boolean process(Packet packet) {
        this.packet = packet;
        str = packet.readString();
        return true;
    }

    @Override
    public void handle() {
        if (connection.getRemoteAddressTCP() != null) {
            InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
            String hostAddress = inetSocketAddress.getAddress().getHostAddress();

            if (connection.getClient() != null) {
                Client client = connection.getClient();
                Map<String, Boolean> files = AntiCheatManager.getInstance().getFilesByClient(client);

                str = str.replace("/", "\\");
                List<String> result = Arrays.asList(str.split(";"));

                if (result.size() == 3) {
                    synchronized (files) {
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
}
