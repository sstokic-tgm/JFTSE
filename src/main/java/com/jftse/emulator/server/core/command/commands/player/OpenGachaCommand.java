package com.jftse.emulator.server.core.command.commands.player;

import com.jftse.emulator.server.core.command.Command;
import com.jftse.emulator.server.core.manager.ThreadManager;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.task.GachaMachineTask;
import com.jftse.emulator.server.networking.Connection;

import java.util.List;

public class OpenGachaCommand extends Command {

    public OpenGachaCommand() {
        setDescription("Open gacha with given amount");
    }

    @Override
    public void execute(Connection connection, List<String> params) {
        if ((params.size() < 2 && connection.getClient().getActiveRoom() != null) || (params.size() < 2 && connection.getClient().getActiveRoom() == null)) {
            S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Use -og <\"gacha name\"> <number>");
            connection.sendTCP(chatLobbyAnswerPacket);
            return;
        }

        if (!connection.getClient().getUsingGachaMachine().get()) {
            ThreadManager.getInstance().newTask(new GachaMachineTask(connection, params));
        }
    }
}
