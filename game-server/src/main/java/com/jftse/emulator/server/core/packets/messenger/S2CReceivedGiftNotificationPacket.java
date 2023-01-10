package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.Gift;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CReceivedGiftNotificationPacket extends Packet {
    public S2CReceivedGiftNotificationPacket(Gift gift) {
        super(PacketOperations.S2CReceivedGiftNotification);

        this.write(gift.getId().intValue());
        this.write(gift.getSender().getName());
        this.write(gift.getSeen());
        this.write(gift.getMessage());
        this.write(gift.getCreated());
        this.write(gift.getProduct().getProductIndex());
        this.write(gift.getUseTypeOption());
    }
}
