package com.jftse.emulator.server.core.packets.enchant;

import com.jftse.server.core.enchant.EnchantResultMessage;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CEnchantAnswerPacket extends Packet {
    public S2CEnchantAnswerPacket(EnchantResultMessage enchantResult) {
        super(PacketOperations.S2CEnchantAnswer);

        this.write(enchantResult.getCode());
    }
}
