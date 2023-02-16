package com.jftse.emulator.server.core.command.commands.player;

import com.jftse.emulator.server.core.command.Command;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.constants.GameMode;

import java.util.List;

public class RandomModeCommand extends Command {

    public RandomModeCommand() {
        setDescription("Random guardians will spawn (boss stay same)");
    }

    @Override
    public void execute(FTConnection connection, List<String> params) {
        if (connection.getClient().getActiveRoom() == null || connection.getClient().getRoomPlayer() == null)
            return;

        Room room = connection.getClient().getActiveRoom();
        RoomPlayer roomPlayer = connection.getClient().getRoomPlayer();

        boolean isGuardian = GameManager.getInstance().getRoomMode(room) == GameMode.GUARDIAN;
        if (!isGuardian)
            return;

        if (roomPlayer.isMaster()) {
            if (!GameManager.getInstance().isAllowedToChangeMode(room)) {
                S2CChatRoomAnswerPacket randomGuardianChangedPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", "All in the room must be lvl 60 to be able to change modes");
                GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(randomGuardianChangedPacket));
            } else {
                synchronized (room) {
                    room.setRandomGuardians(!room.isRandomGuardians());
                }

                S2CChatRoomAnswerPacket randomGuardianChangedPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", String.format("Random mode %s", room.isRandomGuardians() ? "ON" : "OFF"));
                GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(randomGuardianChangedPacket));
            }
        }
    }
}
