package com.jftse.emulator.server.core.packets.authserver;

import com.jftse.entities.database.model.gameserver.GameServer;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.List;

public class S2CGameServerListPacket extends Packet {

    public S2CGameServerListPacket(List<GameServer> gameServerList) {
        super(PacketOperations.S2CGameServerList);

        this.write((byte) gameServerList.size());

        for (GameServer gameServer : gameServerList) {
            this.write((byte) Math.toIntExact(gameServer.getId()));
            this.write((short) Math.toIntExact(gameServer.getId()));
            this.write(gameServer.getGameServerType().getType());
            this.write(gameServer.getHost());
            this.write((short) gameServer.getPort().intValue());
            this.write((short) 0);  // players online < 75 Good, < 100 Crowded, >= 100 Full
            this.write(gameServer.getIsCustomChannel());
            this.write(gameServer.getName());
        }
    }
}
