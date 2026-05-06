package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.emulator.server.core.client.EquippedItemParts;
import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.EquippedItemStats;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.Map;

public class S2CInventoryWearClothAnswerPacket extends Packet {
    public S2CInventoryWearClothAnswerPacket(char status, FTPlayer player) {
        super(PacketOperations.S2CInventoryWearClothAnswer);

        this.write(status); // status

        if (status == 0) {
            EquippedItemParts equippedItemParts = player.getItemPartsPPId();
            EquippedItemStats equippedItemStats = player.getItemStats();

            this.write(equippedItemParts.hair());
            this.write(equippedItemParts.face());
            this.write(equippedItemParts.dress());
            this.write(equippedItemParts.pants());
            this.write(equippedItemParts.socks());
            this.write(equippedItemParts.shoes());
            this.write(equippedItemParts.gloves());
            this.write(equippedItemParts.racket());
            this.write(equippedItemParts.glasses());
            this.write(equippedItemParts.bag());
            this.write(equippedItemParts.hat());
            this.write(equippedItemParts.dye());

            this.write((BattleUtils.calculatePlayerHp(player.getLevel()) + equippedItemStats.getAddHp()));

            // status points
            this.write((byte) player.getStrength());
            this.write((byte) player.getStamina());
            this.write((byte) player.getDexterity());
            this.write((byte) player.getWillpower());
            // enchant added status points
            this.write((byte) (equippedItemStats.getEnchantStr() + equippedItemStats.getStrength()));
            this.write((byte) (equippedItemStats.getEnchantSta() + equippedItemStats.getStamina()));
            this.write((byte) (equippedItemStats.getEnchantDex() + equippedItemStats.getDexterity()));
            this.write((byte) (equippedItemStats.getEnchantWil() + equippedItemStats.getWillpower()));
            // ??
            for (int i = 5; i < 13; i++) {
                this.write((byte) 0);
            }
            // element??
            this.write((byte) 0);
            this.write((byte) 0);

            // earrings added status points
            this.write(0);
            this.write((byte) 0);
            this.write((byte) 0);
            this.write((byte) 0);
            this.write((byte) 0);
            // cards added status points
            this.write(0);
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

    public S2CInventoryWearClothAnswerPacket(char status, Map<String, Integer> inventoryEquippedCloths, Player player, EquippedItemStats equippedItemStats) {
        super(PacketOperations.S2CInventoryWearClothAnswer);

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

            this.write((BattleUtils.calculatePlayerHp(player.getLevel()) + equippedItemStats.getAddHp()));

            // status points
            this.write(player.getStrength());
            this.write(player.getStamina());
            this.write(player.getDexterity());
            this.write(player.getWillpower());
            // enchant added status points
            this.write((byte) (equippedItemStats.getEnchantStr() + equippedItemStats.getStrength()));
            this.write((byte) (equippedItemStats.getEnchantSta() + equippedItemStats.getStamina()));
            this.write((byte) (equippedItemStats.getEnchantDex() + equippedItemStats.getDexterity()));
            this.write((byte) (equippedItemStats.getEnchantWil() + equippedItemStats.getWillpower()));
            // ??
            for (int i = 5; i < 13; i++) {
                this.write((byte) 0);
            }
            // element??
            this.write((byte) 0);
            this.write((byte) 0);

            // earrings added status points
            this.write(0);
            this.write((byte) 0);
            this.write((byte) 0);
            this.write((byte) 0);
            this.write((byte) 0);
            // cards added status points
            this.write(0);
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
