package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.entities.database.model.player.EquippedItemStats;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CGameEndLevelUpPlayerStatsPacket extends Packet {
    public S2CGameEndLevelUpPlayerStatsPacket(short playerPosition, FTPlayer player) {
        super(PacketOperations.S2CGameEndLevelUpPlayerStats);

        EquippedItemStats equippedItemStats = player.getItemStats();

        this.write(playerPosition); // not sure if it's the pos
        this.write((byte) player.getLevel());

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