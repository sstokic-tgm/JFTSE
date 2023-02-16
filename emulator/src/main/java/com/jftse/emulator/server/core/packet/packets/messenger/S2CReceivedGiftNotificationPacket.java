package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.entities.database.model.messenger.Gift;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CReceivedGiftNotificationPacket extends Packet {
    public S2CReceivedGiftNotificationPacket(Gift gift) {
        super(PacketOperations.S2CReceivedGiftNotification.getValueAsChar());

        this.write(gift.getId().intValue());
        this.write(gift.getSender().getName());
        this.write(gift.getSeen());
        this.write(gift.getMessage());
        this.write(gift.getCreated());
        this.write(gift.getProduct().getProductIndex());
        this.write(gift.getUseTypeOption());
    }
}
