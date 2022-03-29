package com.jftse.emulator.server.core.command.commands.gm;

import com.jftse.emulator.server.core.command.Command;
import com.jftse.emulator.server.core.manager.ServerManager;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class ServerNoticeCommand extends Command {

    public ServerNoticeCommand() {
        setDescription("Sets and sends a server notice");
    }

    @Override
    public void execute(Connection connection, List<String> params) {
        if (params.size() < 1) {
            Packet answer;
            if (connection.getClient().isInLobby())
                answer = new S2CChatLobbyAnswerPacket((char) 0, "Command", "Use -sN <\"message\"> to set a server notice");
            else
                answer = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Use -sN <\"message\"> to set a server notice");
            connection.sendTCP(answer);

            ServerManager.getInstance().setServerNoticeIsSet(false);
            ServerManager.getInstance().broadcastServerNotice("", -1);
            return;
        }

        String message = params.get(0);
        ServerManager.getInstance().broadcastServerNotice(message, 0);
    }
}
