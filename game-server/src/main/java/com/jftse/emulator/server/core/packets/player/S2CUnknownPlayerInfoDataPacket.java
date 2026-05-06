package com.jftse.emulator.server.core.packets.player;

import com.jftse.emulator.server.core.client.EquippedItemParts;
import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.client.GuildView;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.entities.database.model.player.EquippedItemStats;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CUnknownPlayerInfoDataPacket extends Packet {
    public S2CUnknownPlayerInfoDataPacket(FTPlayer player, Pocket pocket, PlayerStatistic playerStatistic) {
        super(PacketOperations.S2CUnknownPlayerInfoData);

        EquippedItemParts equippedItemParts = player.getItemPartsPPId();
        EquippedItemStats equippedItemStats = player.getItemStats();

        this.write(player.getName());

        GuildView guild = player.getGuild();
        if (guild != null) {
            this.write(guild.logoBackgroundId());
            this.write(guild.logoBackgroundColor());
            this.write(guild.logoPatternId());
            this.write(guild.logoPatternColor());
            this.write(guild.logoMarkId());
            this.write(guild.logoMarkColor());
        } else {
            for (int i = 0; i < 6; i++)
                this.write(0);
        }

        this.write(playerStatistic.getBasicRecordWin());
        this.write(playerStatistic.getBasicRecordLoss());
        this.write(playerStatistic.getBattleRecordWin());
        this.write(playerStatistic.getBattleRecordLoss());
        this.write(playerStatistic.getConsecutiveWins());
        this.write(0); // ??
        this.write(playerStatistic.getNumberOfDisconnects());
        this.write(playerStatistic.getTotalGames());
        this.write(0); // ??
        this.write(0); // ??

        this.write((byte) 0); // ??

        this.write(player.getExpPoints());
        this.write(0); // perfect(s)
        this.write(0); // guard break(s)

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
        this.write((byte) player.getStatusPoints());

        this.write(-1); // active pet type
        this.write(pocket.getMaxBelongings().shortValue());
        this.write((byte) 0); // card slots

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

        this.write(player.getCouplePoints());
        this.write(0); // ??
    }
}
