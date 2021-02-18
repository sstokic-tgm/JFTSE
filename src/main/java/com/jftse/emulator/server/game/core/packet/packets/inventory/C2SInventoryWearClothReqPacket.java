package com.jftse.emulator.server.game.core.packet.packets.inventory;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SInventoryWearClothReqPacket extends Packet {
    private int hair;
    private int face;
    private int dress;
    private int pants;
    private int socks;
    private int shoes;
    private int gloves;
    private int racket;
    private int glasses;
    private int bag;
    private int hat;
    private int dye;

    public C2SInventoryWearClothReqPacket(Packet packet) {
        super(packet);

        this.hair = this.readInt();
        this.face = this.readInt();
        this.dress = this.readInt();
        this.pants = this.readInt();
        this.socks = this.readInt();
        this.shoes = this.readInt();
        this.gloves = this.readInt();
        this.racket = this.readInt();
        this.glasses = this.readInt();
        this.bag = this.readInt();
        this.hat = this.readInt();
        this.dye = this.readInt();
    }
}
