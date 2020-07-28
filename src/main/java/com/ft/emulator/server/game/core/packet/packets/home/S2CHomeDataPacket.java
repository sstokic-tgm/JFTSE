package com.ft.emulator.server.game.core.packet.packets.home;

import com.ft.emulator.server.database.model.home.AccountHome;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CHomeDataPacket extends Packet {
    public S2CHomeDataPacket(AccountHome accountHome) {
        super(PacketID.S2CHomeData);

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
