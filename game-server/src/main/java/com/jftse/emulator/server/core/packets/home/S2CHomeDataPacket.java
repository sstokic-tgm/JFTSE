package com.jftse.emulator.server.core.packets.home;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.entities.database.model.home.AccountHome;

public class S2CHomeDataPacket extends Packet {
    public S2CHomeDataPacket(AccountHome accountHome) {
        super(PacketOperations.S2CHomeData);

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
