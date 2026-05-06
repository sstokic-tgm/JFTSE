package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.anticheat.ClientWhitelist;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.ClientWhitelistService;
import com.jftse.server.core.shared.packets.ac.CMSGAntiCheatClientRegister;
import com.jftse.server.core.shared.packets.ac.SMSGAntiCheatClientRegister;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@PacketId(CMSGAntiCheatClientRegister.PACKET_ID)
public class ACClientRegisterHandler implements PacketHandler<FTConnection, CMSGAntiCheatClientRegister> {
    private final ClientWhitelistService clientWhitelistService;

    public ACClientRegisterHandler() {
        clientWhitelistService = ServiceManager.getInstance().getClientWhitelistService();
    }

    @Override
    public void handle(FTConnection connection, CMSGAntiCheatClientRegister packet) {
        String str = packet.getStr();

        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        if (inetSocketAddress != null) {
            String hostAddress = inetSocketAddress.getAddress().getHostAddress();

            FTClient client = connection.getClient();
            if (client != null) {
                ConcurrentHashMap<String, Boolean> files = client.getFileList();

                str = str.replace("/", "\\");
                List<String> result = Arrays.asList(str.split(";"));

                if (result.size() == 3) {
                    files.entrySet().stream()
                            .filter(f -> result.containsAll(Arrays.asList(f.getKey().split(";"))))
                            .findFirst()
                            .ifPresent(f -> f.setValue(true));
                    boolean valid = files.entrySet().stream().allMatch(Map.Entry::getValue);

                    if (!valid) {
                        SMSGAntiCheatClientRegister response = SMSGAntiCheatClientRegister.builder().result((byte) 1).build();
                        connection.sendTCP(response);
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

                            SMSGAntiCheatClientRegister response = SMSGAntiCheatClientRegister.builder().result((byte) 0).build();
                            connection.sendTCP(response);
                        }
                    } else {
                        SMSGAntiCheatClientRegister response = SMSGAntiCheatClientRegister.builder().result((byte) 2).build();
                        connection.sendTCP(response);
                    }
                }
            }
        }
    }
}
