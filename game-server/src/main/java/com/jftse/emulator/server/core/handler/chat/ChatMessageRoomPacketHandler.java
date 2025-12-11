package com.jftse.emulator.server.core.handler.chat;

import com.jftse.emulator.server.core.command.CommandManager;
import com.jftse.emulator.server.core.constants.MiscConstants;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.chat.CMSGChatMessageRoom;
import com.jftse.server.core.shared.packets.chat.SMSGChatMessageRoom;

@PacketId(CMSGChatMessageRoom.PACKET_ID)
public class ChatMessageRoomPacketHandler implements PacketHandler<FTConnection, CMSGChatMessageRoom> {
    @Override
    public void handle(FTConnection connection, CMSGChatMessageRoom chatRoomReqPacket) {
        FTClient client = connection.getClient();
        final Room room = client.getActiveRoom();
        if (room == null)
            return;

        final Player player = client.getPlayer();
        final RoomPlayer roomPlayer = client.getRoomPlayer();

        boolean playerInSecretGmSlot = roomPlayer != null && roomPlayer.getPosition() == MiscConstants.InvisibleGmSlot;
        byte messageType = playerInSecretGmSlot ? (byte) 2 : chatRoomReqPacket.getType();
        SMSGChatMessageRoom chatRoomMessage = SMSGChatMessageRoom.builder()
                .type(messageType)
                .sender(player.getName())
                .message(chatRoomReqPacket.getMessage())
                .build();

        if (CommandManager.getInstance().isCommand(chatRoomReqPacket.getMessage())) {
            connection.sendTCP(chatRoomMessage);
            CommandManager.getInstance().handle(connection, chatRoomReqPacket.getMessage());
            return;
        }

        boolean isTeamChat = chatRoomReqPacket.getType() == 1;
        if (isTeamChat && roomPlayer != null) {
            short senderPos = roomPlayer.getPosition();

            if (senderPos < 0) return;
            for (FTClient c : GameManager.getInstance().getClientsInRoom(room.getRoomId())) {
                RoomPlayer rp = c.getRoomPlayer();
                if (rp == null)
                    continue;

                boolean playerCanSeeMessage = areInSameTeam(senderPos, rp.getPosition()) || rp.getPosition() == MiscConstants.InvisibleGmSlot;
                if (c.getPlayer() != null && c.getPlayer().getId().equals(rp.getPlayerId()) && playerCanSeeMessage) {
                    c.getConnection().sendTCP(chatRoomMessage);
                }
            }
            connection.sendTCP(chatRoomMessage); // Send to sender
        } else {
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(chatRoomMessage));
        }
    }

    private boolean areInSameTeam(int playerPos1, int playerPos2) {
        boolean bothInRedTeam = (playerPos1 == 0 && playerPos2 == 2) || (playerPos1 == 2 && playerPos2 == 0);
        boolean bothInBlueTeam = (playerPos1 == 1 && playerPos2 == 3) || (playerPos1 == 3 && playerPos2 == 1);
        return bothInRedTeam || bothInBlueTeam;
    }
}
