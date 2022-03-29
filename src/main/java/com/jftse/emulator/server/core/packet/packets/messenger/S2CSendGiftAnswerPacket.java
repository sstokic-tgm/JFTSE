package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.database.model.messenger.Gift;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CSendGiftAnswerPacket extends Packet {
    public S2CSendGiftAnswerPacket(short status, Gift gift) {
        super(PacketOperations.S2CSendGiftAnswer.getValueAsChar());

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
