package com.jftse.emulator.server.core.packets.item;

import com.jftse.server.core.protocol.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class C2SResponseItemSettingsPacket extends Packet {
    private byte size;
    private List<ItemSettings> itemSettings;

    public C2SResponseItemSettingsPacket(Packet packet) {
        super(packet);

        this.itemSettings = new ArrayList<>();

        this.size = this.readByte();
        for (int i = 0; i < this.size; i++) {
            final byte damage = this.readByte();
            final byte damageRate = this.readByte();
            final byte coolingTime = this.readByte();
            final byte gdCoolingTime = this.readByte();
            final byte shotCnt = this.readByte();
            this.itemSettings.add(new ItemSettings(i + 1L, damage, damageRate, coolingTime, gdCoolingTime, shotCnt));
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ItemSettings {
        private Long id;
        private byte damage;
        private byte damageRate;
        private byte coolingTime;
        private byte gdCoolingTime;
        private byte shotCnt;
    }
}
