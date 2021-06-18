package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.database.model.messaging.Message;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CSendGiftAnswerPacket extends Packet {
    public S2CSendGiftAnswerPacket(short status) {
        super(PacketID.S2CSendGiftAnswer);

        this.write(status);
    }
}
