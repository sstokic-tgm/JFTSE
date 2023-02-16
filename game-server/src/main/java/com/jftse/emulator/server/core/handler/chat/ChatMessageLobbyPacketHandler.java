package com.jftse.emulator.server.core.handler.chat;

import com.jftse.emulator.server.core.command.CommandManager;
import com.jftse.emulator.server.core.packets.chat.C2SChatLobbyReqPacket;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.List;
import java.util.stream.Collectors;

@PacketOperationIdentifier(PacketOperations.C2SChatLobbyReq)
public class ChatMessageLobbyPacketHandler extends AbstractPacketHandler {
    private C2SChatLobbyReqPacket chatLobbyReqPacket;

    @Override
    public boolean process(Packet packet) {
        chatLobbyReqPacket = new C2SChatLobbyReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket(chatLobbyReqPacket.getUnk(), client.getPlayer().getName(), chatLobbyReqPacket.getMessage());

        if (CommandManager.getInstance().isCommand(chatLobbyReqPacket.getMessage())) {
            connection.sendTCP(chatLobbyAnswerPacket);
            CommandManager.getInstance().handle((FTConnection) connection, chatLobbyReqPacket.getMessage());
            return;
        }

        List<FTClient> clientList = GameManager.getInstance().getClients().stream()
                .filter(FTClient::isInLobby)
                .collect(Collectors.toList());

        clientList.forEach(c -> c.getConnection().sendTCP(chatLobbyAnswerPacket));
    }
}
