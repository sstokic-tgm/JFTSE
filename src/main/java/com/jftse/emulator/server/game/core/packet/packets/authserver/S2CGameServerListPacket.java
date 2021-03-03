package com.jftse.emulator.server.game.core.packet.packets.authserver;

import com.jftse.emulator.server.database.model.gameserver.GameServer;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CGameServerListPacket extends Packet {

    public S2CGameServerListPacket(List<GameServer> gameServerList) {
        super(PacketID.S2CGameServerList);

        this.write((byte) gameServerList.size());

        for (GameServer gameServer : gameServerList) {
            this.write((byte) 0);  // ?
            this.write((short) Math.toIntExact(gameServer.getId()));
            this.write(gameServer.getGameServerType().getType());
            this.write(gameServer.getHost());
            this.write((short) gameServer.getPort().intValue());
            this.write((short) 0);  // players online
            this.write(gameServer.getIsCustomChannel());
            this.write(gameServer.getName());
        }
    }
}
