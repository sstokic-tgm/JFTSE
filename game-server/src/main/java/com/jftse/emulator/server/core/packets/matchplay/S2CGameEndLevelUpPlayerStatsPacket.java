package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;

public class S2CGameEndLevelUpPlayerStatsPacket extends Packet {
    public S2CGameEndLevelUpPlayerStatsPacket(short playerPosition, Player player, StatusPointsAddedDto statusPointsAddedDto) {
        super(PacketOperations.S2CGameEndLevelUpPlayerStats.getValue());

        this.write(playerPosition); // not sure if it's the pos
        this.write(player.getLevel());
        this.write(BattleUtils.calculatePlayerHp(player.getLevel()));

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