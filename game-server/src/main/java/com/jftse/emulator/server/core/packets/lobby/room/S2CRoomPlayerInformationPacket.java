package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.emulator.server.core.client.EquippedItemParts;
import com.jftse.emulator.server.core.client.GuildView;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.entities.database.model.player.EquippedItemStats;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomPlayerInformationPacket extends Packet {
    public S2CRoomPlayerInformationPacket(RoomPlayer roomPlayer, float spawnX, float spawnY, float spawnX2, float spawnY2, int lastMapLayer) {
        super(PacketOperations.S2CRoomPlayerInformationWithPosition);

        EquippedItemParts equippedItemParts = roomPlayer.getEquippedItemPartsIDX();
        EquippedItemStats equippedItemStats = roomPlayer.getEquippedItemStats();

        boolean isSpectator = roomPlayer.getPosition() > 3;

        this.write(roomPlayer.getPosition());
        this.write(roomPlayer.getName());
        this.write((byte) roomPlayer.getLevel());
        this.write(roomPlayer.isGameMaster());
        this.write(roomPlayer.isMaster());
        this.write(roomPlayer.isReady());
        this.write(roomPlayer.isFitting());
        this.write((byte) roomPlayer.getPlayerType());
        this.write(isSpectator);
        this.write((byte) 0); // unk3

        GuildView guild = roomPlayer.getGuild();
        this.write(guild != null ? guild.name() : "");

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

        this.write((byte) 0);
        this.write(roomPlayer.getCoupleName());
        this.write(0);
        this.write((byte) 0);
        this.write((short) 0); // emblem slot 1
        this.write((short) 0); // emblem slot 2
        this.write((short) 0); // emblem slot 3
        this.write((short) 0); // emblem slot 4

        this.write((BattleUtils.calculatePlayerHp(roomPlayer.getLevel()) + equippedItemStats.getAddHp()));

        // status points
        this.write((byte) roomPlayer.getStrength());
        this.write((byte) roomPlayer.getStamina());
        this.write((byte) roomPlayer.getDexterity());
        this.write((byte) roomPlayer.getWillpower());
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
        /* end - status points */

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

        // house / town square
        this.write(spawnX);
        this.write(spawnY);

        // square
        this.write(spawnX2);
        this.write(spawnY2);

        this.write((byte) 0);
        this.write(lastMapLayer);
    }
}
