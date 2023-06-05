package com.jftse.emulator.server.core.command.commands.gm;

import com.jftse.emulator.server.core.command.AbstractCommand;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.shared.packets.S2CServerNoticePacket;

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

            S2CServerNoticePacket serverNoticePacket = new S2CServerNoticePacket("");
            GameManager.getInstance().getClients().forEach(client -> {
                if (client.getConnection() != null)
                    client.getConnection().sendTCP(serverNoticePacket);
            });
            return;
        }

        String message = params.get(0);
        S2CServerNoticePacket serverNoticePacket = new S2CServerNoticePacket(message);
        GameManager.getInstance().getClients().forEach(client -> {
            if (client.getConnection() != null)
                client.getConnection().sendTCP(serverNoticePacket);
        });
    }
}
