package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;

public class S2CGameEndLevelUpPlayerStatsPacket extends Packet {
    public S2CGameEndLevelUpPlayerStatsPacket(short playerPosition, Player player, StatusPointsAddedDto statusPointsAddedDto) {
        super(PacketOperations.S2CGameEndLevelUpPlayerStats);

        this.write(playerPosition); // not sure if it's the pos
        this.write(player.getLevel());

        this.write((BattleUtils.calculatePlayerHp(player.getLevel()) + statusPointsAddedDto.getAddHp()));

        // status points
        this.write(player.getStrength());
        this.write(player.getStamina());
        this.write(player.getDexterity());
        this.write(player.getWillpower());
        // enchant added status points
        this.write((byte) (statusPointsAddedDto.getAddStr() + statusPointsAddedDto.getStrength()));
        this.write((byte) (statusPointsAddedDto.getAddSta() + statusPointsAddedDto.getStamina()));
        this.write((byte) (statusPointsAddedDto.getAddDex() + statusPointsAddedDto.getDexterity()));
        this.write((byte) (statusPointsAddedDto.getAddWil() + statusPointsAddedDto.getWillpower()));
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