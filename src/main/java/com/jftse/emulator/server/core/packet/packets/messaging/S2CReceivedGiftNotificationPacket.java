package com.jftse.emulator.server.core.packet.packets.messaging;

import com.jftse.emulator.server.database.model.messaging.Gift;
import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CReceivedGiftNotificationPacket extends Packet {
    public S2CReceivedGiftNotificationPacket(Gift gift) {
        super(PacketID.S2CReceivedGiftNotification);

        this.write(gift.getId().intValue());
        this.write(gift.getSender().getName());
        this.write(gift.getSeen());
        this.write(gift.getMessage());
        this.write(gift.getCreated());
        this.write(gift.getProduct().getProductIndex());
        this.write(gift.getUseTypeOption());
    }
}
