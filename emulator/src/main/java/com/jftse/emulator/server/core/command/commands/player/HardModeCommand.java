package com.jftse.emulator.server.core.command.commands.player;

import com.jftse.emulator.server.core.command.Command;
import com.jftse.emulator.server.core.constants.GameMode;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.networking.Connection;

import java.util.List;

public class HardModeCommand extends Command {

    public HardModeCommand() {
        setDescription("Gives every guardian a boost on their stats and spawns 3 guardians");
    }

    @Override
    public void execute(Connection connection, List<String> params) {
        if (connection.getClient().getActiveRoom() == null || connection.getClient().getRoomPlayer() == null)
            return;

        Room room = connection.getClient().getActiveRoom();
        RoomPlayer roomPlayer = connection.getClient().getRoomPlayer();

        boolean isGuardian = GameManager.getInstance().getRoomMode(room) == GameMode.GUARDIAN;
        if (!isGuardian)
            return;

        if (roomPlayer.isMaster()) {
            if (!GameManager.getInstance().isAllowedToChangeMode(room)) {
                S2CChatRoomAnswerPacket hardModeChangedPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", "All in the room must be lvl 60 to be able to change modes");
                GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().getServer().sendToTcp(c.getConnection().getId(), hardModeChangedPacket));
            } else {
                synchronized (room) {
                    room.setHardMode(!room.isHardMode());
                }

                S2CChatRoomAnswerPacket hardModeChangedPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", String.format("Hard mode %s", room.isHardMode() ? "ON" : "OFF"));
                GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().getServer().sendToTcp(c.getConnection().getId(), hardModeChangedPacket));
            }
        }
    }
}
