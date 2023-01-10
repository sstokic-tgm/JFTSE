package com.jftse.emulator.server.core.command.commands.player;

import com.jftse.emulator.server.core.command.Command;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.task.GachaMachineTask;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.thread.ThreadManager;

import java.util.List;

public class OpenGachaCommand extends Command {

    public OpenGachaCommand() {
        setDescription("Open gacha with given amount");
    }

    @Override
    public void execute(FTConnection connection, List<String> params) {
        if (connection.getClient() == null)
            return;

        if ((params.size() < 2 && connection.getClient().getActiveRoom() != null) || (params.size() < 2 && connection.getClient().getActiveRoom() == null)) {
            S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Use -og <\"gacha name\"> <number>");
            connection.sendTCP(chatLobbyAnswerPacket);
            return;
        }

        final boolean usingGachaMachine = connection.getClient().isUsingGachaMachine();
        if (!usingGachaMachine) {
            ThreadManager.getInstance().newTask(new GachaMachineTask(connection, params));
        }
    }
}
