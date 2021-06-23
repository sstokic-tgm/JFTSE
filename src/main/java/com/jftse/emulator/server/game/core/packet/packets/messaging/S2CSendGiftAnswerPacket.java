package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.database.model.messaging.Gift;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CSendGiftAnswerPacket extends Packet {
    public S2CSendGiftAnswerPacket(short status, Gift gift) {
        super(PacketID.S2CSendGiftAnswer);

        this.write(status);
        if (status == 0) {
            this.write(gift.getId().intValue());
            this.write(gift.getReceiver().getName());
            this.write(gift.getMessage());
            this.write(gift.getCreated());
            this.write(gift.getProduct().getProductIndex());
            this.write(gift.getUseTypeOption());
        }
    }
}
