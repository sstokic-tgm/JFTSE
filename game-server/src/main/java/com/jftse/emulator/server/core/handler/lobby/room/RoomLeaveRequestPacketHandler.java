package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomLeave;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomLeave;

@PacketId(CMSGRoomLeave.PACKET_ID)
public class RoomLeaveRequestPacketHandler implements PacketHandler<FTConnection, CMSGRoomLeave> {
    @Override
    public void handle(FTConnection connection, CMSGRoomLeave packet) {
        FTClient client = connection.getClient();

        if (!client.getIsJoiningOrLeavingRoom().compareAndSet(false, true)) {
            return;
        }

        client.setLobbyCurrentRoomListPage(-1);

        GameManager.getInstance().handleRoomPlayerChanges(client.getConnection(), true);

        SMSGRoomLeave answer = SMSGRoomLeave.builder().result((short) 0).build();
        connection.sendTCP(answer);

        client.getIsJoiningOrLeavingRoom().set(false);

        //GameManager.getInstance().handleChatLobbyJoin(client);
    }
}
