package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.Parcel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class S2CParcelListPacket extends Packet {
    public S2CParcelListPacket(byte listType, List<Parcel> parcels) {
        super(PacketOperations.S2CParcelListAnswer);

        this.write(listType);
        this.write((byte) parcels.size());
        for (Parcel parcel : parcels) {
            this.write(parcel.getId().intValue());
            this.write(listType == (byte) 0 ? parcel.getSender().getName() : parcel.getReceiver().getName());
            this.write(parcel.getMessage());

            EItemCategory category = EItemCategory.valueOf(parcel.getCategory());
            this.write(category.getValue());
            this.write(parcel.getItemIndex());
            this.write(parcel.getUseType().equals("N/A") ? (byte) 0 : EItemUseType.valueOf(parcel.getUseType().toUpperCase()).getValue());
            this.write(parcel.getItemCount());
            this.write(parcel.getEParcelType().getValue());
            this.write(parcel.getGold());
            this.write((byte) 0); // UNK
            this.write(parcel.getCreated());

            this.write(parcel.getEnchantStr().byteValue()); // enchant str
            this.write(parcel.getEnchantSta().byteValue()); // enchant sta
            this.write(parcel.getEnchantDex().byteValue()); // enchant dex
            this.write(parcel.getEnchantWil().byteValue()); // enchant wil

            this.write(parcel.getEnchantElement().byteValue()); // enchant element type 5=earth, 6=wind, 7=water, 8=fire
            this.write(parcel.getEnchantLevel().byteValue()); // enchant element level
        }
    }
}
