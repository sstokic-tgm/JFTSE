package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.database.model.messaging.Parcel;
import com.jftse.emulator.server.game.core.item.EItemCategory;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class S2CSentParcelListPacket extends Packet {
    public S2CSentParcelListPacket(List<Parcel> parcels) {
        super(PacketID.S2CSentParcelList);

        this.write((byte) 1); // TYPE 1 = Received parcels, 1 = Sent parcels, 2 = Received proposals, 3 = Sent proposals
        this.write((byte) parcels.size());
        for (Parcel parcel : parcels) {
            this.write(parcel.getId().intValue());
            this.write(parcel.getReceiver().getName());
            this.write(parcel.getMessage());

            EItemCategory category = EItemCategory.valueOf(parcel.getCategory());
            this.write(category.getValue());
            this.write(parcel.getItemIndex());
            this.write((byte) 0); //UNK
            this.write(parcel.getItemCount());
            this.write(parcel.getParcelType().getValue());
            this.write(parcel.getGold());
            this.write((byte) 0); // UNK
            this.write(parcel.getCreated());
            this.write((byte) 0); // UNK
            this.write((byte) 0); // UNK
            this.write((byte) 0); // UNK
            this.write((byte) 0); // UNK
            this.write((byte) 0); // UNK
            this.write((byte) 0); // UNK
        }
    }
}
