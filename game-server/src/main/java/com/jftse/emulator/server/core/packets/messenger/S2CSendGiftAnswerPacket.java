package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.Gift;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CSendGiftAnswerPacket extends Packet {
    public S2CSendGiftAnswerPacket(short status, Gift gift) {
        super(PacketOperations.S2CSendGiftAnswer);

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
