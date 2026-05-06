package com.jftse.emulator.server.core.packets.shop;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRedeemCouponPacket extends Packet {
    public static final short SUCCESS = 0;
    public static final short NA_COUPON = -1;
    public static final short ALREADY_USED = -2;
    public static final short EXPIRED = -4;
    public static final short WRONG = -5;

    public S2CRedeemCouponPacket(short result, String couponCode, Integer productIndex, byte useOption) {
        super(PacketOperations.S2CRedeemCouponAnswer);

        this.write(result);
        this.write(couponCode);
        this.write(productIndex);
        this.write(useOption);
    }
}
