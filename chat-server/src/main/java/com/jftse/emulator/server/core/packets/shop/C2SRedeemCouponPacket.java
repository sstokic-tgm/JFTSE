package com.jftse.emulator.server.core.packets.shop;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRedeemCouponPacket extends Packet {
    private String couponCode;

    public C2SRedeemCouponPacket(Packet packet) {
        super(packet);

        this.couponCode = this.readUnicodeString();
    }
}
