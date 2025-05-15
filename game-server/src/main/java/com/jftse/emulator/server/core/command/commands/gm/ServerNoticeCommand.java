package com.jftse.emulator.server.core.command.commands.gm;

import com.jftse.emulator.server.core.command.AbstractCommand;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.shared.rabbit.messages.ServerNoticeMessage;

import java.util.List;

public class ServerNoticeCommand extends AbstractCommand {

    public ServerNoticeCommand() {
        setDescription("Sets and sends a server notice");
    }

    @Override
    public void execute(FTConnection connection, List<String> params) {
        if (params.size() < 1) {
            Packet answer;
            if (connection.getClient().isInLobby())
                answer = new S2CChatLobbyAnswerPacket((char) 0, "Command", "Use -sN <\"message\"> to set a server notice");
            else
                answer = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Use -sN <\"message\"> to set a server notice");
            connection.sendTCP(answer);
            return;
        }

        String message = params.get(0);
        Long accountId = connection.getClient().getAccountId();

        ServerNoticeMessage serverNoticeMessage = ServerNoticeMessage.builder()
                .accountId(accountId)
                .message(message)
                .build();
        RProducerService.getInstance().send(serverNoticeMessage, "system.motd", "Command Requestor");
    }
}
