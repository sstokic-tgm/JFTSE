package com.jftse.emulator.server.core.handler.chat;

import com.jftse.emulator.server.core.command.CommandManager;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.chat.CMSGChatMessageLobby;
import com.jftse.server.core.shared.packets.chat.SMSGChatMessageLobby;

import java.util.List;

@PacketId(CMSGChatMessageLobby.PACKET_ID)
public class ChatMessageLobbyPacketHandler implements PacketHandler<FTConnection, CMSGChatMessageLobby> {
    @Override
    public void handle(FTConnection connection, CMSGChatMessageLobby chatLobbyReqPacket) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer()) {
            return;
        }

        SMSGChatMessageLobby chatLobbyMessage = SMSGChatMessageLobby.builder()
                .unk(chatLobbyReqPacket.getUnk())
                .sender(client.getPlayer().getName())
                .message(chatLobbyReqPacket.getMessage())
                .build();

        if (CommandManager.getInstance().isCommand(chatLobbyReqPacket.getMessage())) {
            connection.sendTCP(chatLobbyMessage);
            CommandManager.getInstance().handle(connection, chatLobbyReqPacket.getMessage());
            return;
        }

        List<FTClient> clientList = GameManager.getInstance().getClients().stream()
                .filter(FTClient::isInLobby)
                .toList();

        clientList.forEach(c -> c.getConnection().sendTCP(chatLobbyMessage));
    }
}
