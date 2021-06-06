package com.jftse.emulator.server.game.core.packet.packets.inventory;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.game.core.utils.BattleUtils;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.Map;

public class S2CInventoryWearClothAnswerPacket extends Packet {
    public S2CInventoryWearClothAnswerPacket(char status, C2SInventoryWearClothReqPacket inventoryWearClothReqPacket, Player player, StatusPointsAddedDto statusPointsAddedDto) {
        super(PacketID.S2CInventoryWearClothAnswer);

        this.write(status); // status

        if (status == 0) {
            this.write(inventoryWearClothReqPacket.getHair());
            this.write(inventoryWearClothReqPacket.getFace());
            this.write(inventoryWearClothReqPacket.getDress());
            this.write(inventoryWearClothReqPacket.getPants());
            this.write(inventoryWearClothReqPacket.getSocks());
            this.write(inventoryWearClothReqPacket.getShoes());
            this.write(inventoryWearClothReqPacket.getGloves());
            this.write(inventoryWearClothReqPacket.getRacket());
            this.write(inventoryWearClothReqPacket.getGlasses());
            this.write(inventoryWearClothReqPacket.getBag());
            this.write(inventoryWearClothReqPacket.getHat());
            this.write(inventoryWearClothReqPacket.getDye());

            this.write(BattleUtils.calculatePlayerHp(player));

            // status points
            this.write(player.getStrength());
            this.write(player.getStamina());
            this.write(player.getDexterity());
            this.write(player.getWillpower());
            // cloth added status points
            this.write(statusPointsAddedDto.getStrength());
            this.write(statusPointsAddedDto.getStamina());
            this.write(statusPointsAddedDto.getDexterity());
            this.write(statusPointsAddedDto.getWillpower());
            // ??
            for (int i = 5; i < 13; i++) {
                this.write((byte) 0);
            }
            // ??
            this.write((byte) 0);
            this.write((byte) 0);
            // add hp
            this.write(0);
            // cloth added status points for shop
            this.write((byte) 0);
            this.write((byte) 0);
            this.write((byte) 0);
            this.write((byte) 0);
            //??
            this.write(statusPointsAddedDto.getAddHp()); // add hp???
            this.write((byte) 0);
            this.write((byte) 0);
            this.write((byte) 0);
            this.write((byte) 0);
            // ??
            for (int i = 5; i < 13; i++) {
                this.write((byte) 0);
            }
            // ??
            for (int i = 5; i < 13; i++) {
                this.write((byte) 0);
            }
        }
    }

    public S2CInventoryWearClothAnswerPacket(char status, Map<String, Integer> inventoryEquippedCloths, Player player, StatusPointsAddedDto statusPointsAddedDto) {
        super(PacketID.S2CInventoryWearClothAnswer);

        this.write(status); // status

        if (status == 0) {
            this.write(inventoryEquippedCloths.get("hair"));
            this.write(inventoryEquippedCloths.get("face"));
            this.write(inventoryEquippedCloths.get("dress"));
            this.write(inventoryEquippedCloths.get("pants"));
            this.write(inventoryEquippedCloths.get("socks"));
            this.write(inventoryEquippedCloths.get("shoes"));
            this.write(inventoryEquippedCloths.get("gloves"));
            this.write(inventoryEquippedCloths.get("racket"));
            this.write(inventoryEquippedCloths.get("glasses"));
            this.write(inventoryEquippedCloths.get("bag"));
            this.write(inventoryEquippedCloths.get("hat"));
            this.write(inventoryEquippedCloths.get("dye"));

            this.write(BattleUtils.calculatePlayerHp(player));

            // status points
            this.write(player.getStrength());
            this.write(player.getStamina());
            this.write(player.getDexterity());
            this.write(player.getWillpower());
            // cloth added status points
            this.write(statusPointsAddedDto.getStrength());
            this.write(statusPointsAddedDto.getStamina());
            this.write(statusPointsAddedDto.getDexterity());
            this.write(statusPointsAddedDto.getWillpower());
            // ??
            for (int i = 5; i < 13; i++) {
                this.write((byte) 0);
            }
            // ??
            this.write((byte) 0);
            this.write((byte) 0);
            // add hp
            this.write(0);
            // cloth added status points for shop
            this.write((byte) 0);
            this.write((byte) 0);
            this.write((byte) 0);
            this.write((byte) 0);
            //??
            this.write(statusPointsAddedDto.getAddHp());
            this.write((byte) 0);
            this.write((byte) 0);
            this.write((byte) 0);
            this.write((byte) 0);
            // ??
            for (int i = 5; i < 13; ++i) {
                this.write((byte) 0);
            }
            // ??
            for (int i = 5; i < 13; ++i) {
                this.write((byte) 0);
            }
        }
    }
}
