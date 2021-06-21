package com.jftse.emulator.server.game.core.matchplay.room;

import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.messaging.Friend;
import com.jftse.emulator.server.database.model.player.ClothEquipment;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomPlayer {
    private Player player;
    private GuildMember guildMember;
    private Friend couple;
    private ClothEquipment clothEquipment;
    private StatusPointsAddedDto statusPointsAddedDto;
    private short position;
    private boolean master;
    private boolean ready;
    private boolean fitting;
    private boolean gameAnimationSkipReady;
}