package com.jftse.emulator.server.core.handler.game.chat;

import com.jftse.emulator.server.core.command.CommandManager;
import com.jftse.emulator.server.core.constants.MiscConstants;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.chat.C2SChatRoomReqPacket;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

public class ChatMessageRoomPacketHandler extends AbstractHandler {
    private C2SChatRoomReqPacket chatRoomReqPacket;

    @Override
    public boolean process(Packet packet) {
        chatRoomReqPacket = new C2SChatRoomReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        final Room room = connection.getClient().getActiveRoom();
        if (room == null)
            return;

        final Player player = connection.getClient().getPlayer();
        final RoomPlayer roomPlayer = connection.getClient().getRoomPlayer();

        boolean playerInSecretGmSlot = roomPlayer != null && roomPlayer.getPosition() == MiscConstants.InvisibleGmSlot;
        byte messageType = playerInSecretGmSlot ? (byte) 2 : chatRoomReqPacket.getType();
        S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket(messageType, player.getName(), chatRoomReqPacket.getMessage());

        if (CommandManager.getInstance().isCommand(chatRoomReqPacket.getMessage())) {
            connection.sendTCP(chatRoomAnswerPacket);
            CommandManager.getInstance().handle(connection, chatRoomReqPacket.getMessage());
            return;
        }

        boolean isTeamChat = chatRoomReqPacket.getType() == 1;
        if (isTeamChat && roomPlayer != null) {
            short senderPos = roomPlayer.getPosition();

            if (senderPos < 0) return;
            for (Client c : GameManager.getInstance().getClientsInRoom(room.getRoomId())) {
                RoomPlayer rp = c.getRoomPlayer();
                if (rp == null)
                    continue;

                boolean playerCanSeeMessage = areInSameTeam(senderPos, rp.getPosition()) || rp.getPosition() == MiscConstants.InvisibleGmSlot;
                if (c.getPlayer() != null && c.getPlayer().getId().equals(rp.getPlayerId()) && playerCanSeeMessage) {
                    c.getConnection().sendTCP(chatRoomAnswerPacket);
                }
            }
            connection.sendTCP(chatRoomAnswerPacket); // Send to sender
        } else {
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(chatRoomAnswerPacket));
        }
    }

    private boolean areInSameTeam(int playerPos1, int playerPos2) {
        boolean bothInRedTeam = (playerPos1 == 0 && playerPos2 == 2) || (playerPos1 == 2 && playerPos2 == 0);
        boolean bothInBlueTeam = (playerPos1 == 1 && playerPos2 == 3) || (playerPos1 == 3 && playerPos2 == 1);
        return bothInRedTeam || bothInBlueTeam;
    }
}
