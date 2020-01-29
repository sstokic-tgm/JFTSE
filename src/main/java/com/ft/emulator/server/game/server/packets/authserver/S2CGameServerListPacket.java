package com.ft.emulator.server.game.server.packets.authserver;

import com.ft.emulator.server.database.model.gameserver.GameServer;
import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

import java.util.List;

public class S2CGameServerListPacket extends Packet {

    public S2CGameServerListPacket(List<GameServer> gameServerList) {

        super(PacketID.S2CGameServerList);

        this.write((byte)gameServerList.size());

        for(GameServer gameServer : gameServerList) {

            this.write((byte)0); // ?
            this.write((short)Math.toIntExact(gameServer.getId()));
            this.write(gameServer.getGameServerType().getType());
            this.write(gameServer.getHost());
            this.write((short)0);
            this.write((short)gameServer.getPort().intValue());
            this.write((short)0); // players online
            this.write((byte)0); // boolean for custom channel name
            this.write("Custom Name");
            this.write((char)0);
	}
    }
}