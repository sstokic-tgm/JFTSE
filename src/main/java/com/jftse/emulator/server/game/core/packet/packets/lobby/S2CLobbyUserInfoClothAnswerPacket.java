package com.jftse.emulator.server.game.core.packet.packets.lobby;

import com.jftse.emulator.server.database.model.player.ClothEquipment;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CLobbyUserInfoClothAnswerPacket extends Packet {
    public S2CLobbyUserInfoClothAnswerPacket(char result, Player player) {
        super(PacketID.S2CLobbyUserInfoClothAnswer);

        this.write(result);
        if (player != null) {
            ClothEquipment clothEquipment = player.getClothEquipment();

            this.write(player.getPlayerType());

            this.write(clothEquipment.getHair());
            this.write(clothEquipment.getFace());
            this.write(clothEquipment.getDress());
            this.write(clothEquipment.getPants());
            this.write(clothEquipment.getSocks());
            this.write(clothEquipment.getShoes());
            this.write(clothEquipment.getGloves());
            this.write(clothEquipment.getRacket());
            this.write(clothEquipment.getGlasses());
            this.write(clothEquipment.getBag());
            this.write(clothEquipment.getHat());
            this.write(clothEquipment.getDye());
        }
    }
}