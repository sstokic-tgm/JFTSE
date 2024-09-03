package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SMatchplayItemRewardPickupRequest extends Packet {
    private byte slot;

    public C2SMatchplayItemRewardPickupRequest(Packet packet) {
        super(packet);

        this.slot = this.readByte();
    }
}
