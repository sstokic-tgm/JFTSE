package com.jftse.emulator.server.core.packets.player;

import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CPlayerNameChangePacket extends Packet {
    public S2CPlayerNameChangePacket(byte result, Player player) {
        super(PacketOperations.S2CPlayerNameChangeAnswer);

        this.write(result);
        if (result == S2CPlayerNameChangeMessagePacket.RESULT_SUCCESS) {
            this.write(Math.toIntExact(player.getId()));
            this.write(player.getName());
        }
    }
}
