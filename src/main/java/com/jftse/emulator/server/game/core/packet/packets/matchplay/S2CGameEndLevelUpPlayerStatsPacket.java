package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.game.core.utils.BattleUtils;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGameEndLevelUpPlayerStatsPacket extends Packet {
    public S2CGameEndLevelUpPlayerStatsPacket(short playerPosition, Player player, StatusPointsAddedDto statusPointsAddedDto) {
        super(PacketID.S2CGameEndLevelUpPlayerStats);

        this.write(playerPosition); // not sure if it's the pos
        this.write(player.getLevel());
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