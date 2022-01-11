package com.jftse.emulator.server.core.handler.game.chat;

import com.jftse.emulator.server.core.command.CommandManager;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packet.packets.chat.C2SChatLobbyReqPacket;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.List;
import java.util.stream.Collectors;

public class ChatMessageLobbyPacketHandler extends AbstractHandler {
    private C2SChatLobbyReqPacket chatLobbyReqPacket;

    @Override
    public boolean process(Packet packet) {
        chatLobbyReqPacket = new C2SChatLobbyReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket(chatLobbyReqPacket.getUnk(), connection.getClient().getActivePlayer().getName(), chatLobbyReqPacket.getMessage());

        if (CommandManager.getInstance().isCommand(chatLobbyReqPacket.getMessage())) {
            connection.sendTCP(chatLobbyAnswerPacket);
            CommandManager.getInstance().handle(connection, chatLobbyReqPacket.getMessage());
            return;
        }

        List<Client> clientList = GameManager.getInstance().getClients().stream()
                .filter(c -> c.isInLobby())
                .collect(Collectors.toList());

        clientList.forEach(c -> c.getConnection().getServer().sendToTcp(c.getConnection().getId(), chatLobbyAnswerPacket));
    }
}
