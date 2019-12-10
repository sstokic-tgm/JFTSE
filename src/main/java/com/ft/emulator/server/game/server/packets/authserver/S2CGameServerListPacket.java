package com.ft.emulator.server.game.server.packets.authserver;

import com.ft.emulator.server.database.model.gameserver.GameServer;
import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

import java.util.List;

public class S2CGameServerListPacket extends Packet {

    public S2CGameServerListPacket(List<GameServer> gameServerList) {

        super(PacketID.S2CGameServerList);

        this.write((short)gameServerList.size());

        for(GameServer gameServer : gameServerList) {

            this.write((short)Math.toIntExact(gameServer.getId()));
            this.write(gameServer.getGameServerType().getType());
            this.write(gameServer.getHost());
            this.write((short)0);
            this.write((short)gameServer.getPort().intValue());
            this.write(0);
	}
    }
}