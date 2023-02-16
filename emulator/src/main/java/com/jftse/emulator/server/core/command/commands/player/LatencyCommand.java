package com.jftse.emulator.server.core.command.commands.player;

import com.jftse.emulator.server.core.command.Command;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class LatencyCommand extends Command {
    public LatencyCommand() {
        setDescription("Prints your current latency to the server");
    }

    @Override
    public void execute(Connection connection, List<String> params) {
        Packet answer;
        if (connection.getClient().isInLobby())
            answer = new S2CChatLobbyAnswerPacket((char) 0, "Command", connection.getLatency() + "ms");
        else
            answer = new S2CChatRoomAnswerPacket((byte) 2, "Room", connection.getLatency() + "ms");
        connection.sendTCP(answer);
    }
}
