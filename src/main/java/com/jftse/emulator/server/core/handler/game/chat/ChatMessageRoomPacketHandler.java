package com.jftse.emulator.server.core.handler.game.chat;

import com.jftse.emulator.server.core.command.CommandManager;
import com.jftse.emulator.server.core.constants.MiscConstants;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.chat.C2SChatRoomReqPacket;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomPlayerInformationPacket;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatMessageRoomPacketHandler extends AbstractHandler {
    private C2SChatRoomReqPacket chatRoomReqPacket;

    @Override
    public boolean process(Packet packet) {
        chatRoomReqPacket = new C2SChatRoomReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Room room = connection.getClient().getActiveRoom();
        if (room == null) return;

        Long playerId = connection.getClient().getPlayer().getId();
        RoomPlayer roomPlayer = room.getRoomPlayerList().stream()
                .filter(rp -> rp.getPlayer().getId() == playerId)
                .findAny()
                .orElse(null);

        boolean playerInSecretGmSlot = roomPlayer != null && roomPlayer.getPosition() == MiscConstants.InvisibleGmSlot;
        byte messageType = playerInSecretGmSlot ? (byte) 2 : chatRoomReqPacket.getType();
        S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket(messageType, connection.getClient().getPlayer().getName(), chatRoomReqPacket.getMessage());

        if (CommandManager.getInstance().isCommand(chatRoomReqPacket.getMessage())) {
            connection.sendTCP(chatRoomAnswerPacket);
            CommandManager.getInstance().handle(connection, chatRoomReqPacket.getMessage());
            return;
        }

        boolean isTeamChat = chatRoomReqPacket.getType() == 1;
        if (isTeamChat) {
            short senderPos = -1;
            for (RoomPlayer rp : room.getRoomPlayerList()) {
                if (connection.getClient().getPlayer().getId().equals(rp.getPlayer().getId())) {
                    senderPos = rp.getPosition();
                    break;
                }
            }

            if (senderPos < 0) return;
            for (Client c : GameManager.getInstance().getClientsInRoom(room.getRoomId())) {
                for (RoomPlayer rp : c.getActiveRoom().getRoomPlayerList()) {
                    boolean playerCanSeeMessage = areInSameTeam(senderPos, rp.getPosition()) || rp.getPosition() == MiscConstants.InvisibleGmSlot;
                    if (c.getPlayer() != null && c.getPlayer().getId().equals(rp.getPlayer().getId()) && playerCanSeeMessage) {
                        c.getConnection().getServer().sendToTcp(c.getConnection().getId(), chatRoomAnswerPacket);
                    }
                }
            }
            connection.sendTCP(chatRoomAnswerPacket); // Send to sender
        } else {
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().getServer().sendToTcp(c.getConnection().getId(), chatRoomAnswerPacket));
        }
    }

    private boolean areInSameTeam(int playerPos1, int playerPos2) {
        boolean bothInRedTeam = (playerPos1 == 0 && playerPos2 == 2) || (playerPos1 == 2 && playerPos2 == 0);
        boolean bothInBlueTeam = (playerPos1 == 1 && playerPos2 == 3) || (playerPos1 == 3 && playerPos2 == 1);
        return bothInRedTeam || bothInBlueTeam;
    }
}
