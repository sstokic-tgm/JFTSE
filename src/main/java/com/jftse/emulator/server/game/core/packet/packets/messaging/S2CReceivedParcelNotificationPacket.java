package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.database.model.messaging.Parcel;
import com.jftse.emulator.server.game.core.item.EItemCategory;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CReceivedParcelNotificationPacket extends Packet {
    public S2CReceivedParcelNotificationPacket(Parcel parcel) {
        super(PacketID.S2CReceivedParcelNotification);

        this.write(parcel.getId().intValue());
        this.write(parcel.getSender().getName());
        this.write(parcel.getCreated());
        this.write(parcel.getMessage());

        EItemCategory category = EItemCategory.valueOf(parcel.getProduct().getCategory());
        this.write(category.getValue());
        this.write(parcel.getProduct().getItem0());
        this.write((byte) 0); //UNK
        this.write(1); //AMOUNT
        this.write((byte) 2); //TYPE 0 = Nothing, 1 = Gold, 2 = Cash on delivery
        this.write(parcel.getCashOnDelivery()); //GOLD
    }
}
