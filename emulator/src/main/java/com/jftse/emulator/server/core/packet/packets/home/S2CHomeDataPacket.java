package com.jftse.emulator.server.core.packet.packets.home;

import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CHomeDataPacket extends Packet {
    public S2CHomeDataPacket(AccountHome accountHome) {
        super(PacketOperations.S2CHomeData.getValueAsChar());

        this.write(accountHome.getLevel());
        this.write(accountHome.getHousingPoints());
        this.write(accountHome.getFamousPoints());
        this.write(accountHome.getFurnitureCount());
        this.write(accountHome.getBasicBonusExp());
        this.write(accountHome.getBasicBonusGold());
        this.write(accountHome.getBattleBonusExp());
        this.write(accountHome.getBattleBonusGold());
    }
}
