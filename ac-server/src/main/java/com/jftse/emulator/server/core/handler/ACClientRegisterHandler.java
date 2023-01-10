package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.anticheat.ClientWhitelist;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.ClientWhitelistService;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@PacketOperationIdentifier(PacketOperations.C2SAntiCheatClientRegister)
public class ACClientRegisterHandler extends AbstractPacketHandler {
    private Packet packet;

    private String str;

    private final ClientWhitelistService clientWhitelistService;

    public ACClientRegisterHandler() {
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

            FTClient client = (FTClient) connection.getClient();
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
