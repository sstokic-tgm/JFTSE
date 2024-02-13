package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.player.ClothEquipment;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;

import java.util.List;

public class S2CRoomPlayerListInformationPacket extends Packet {
    public S2CRoomPlayerListInformationPacket(List<RoomPlayer> roomPlayerList) {
        super(PacketOperations.S2CRoomPlayerInformation);

        this.write((char) roomPlayerList.size());
        for (RoomPlayer roomPlayer : roomPlayerList) {
            Guild guild = null;
            GuildMember guildMember = roomPlayer.getGuildMember();
            if (guildMember != null && !guildMember.getWaitingForApproval() && guildMember.getGuild() != null)
                guild = guildMember.getGuild();

            Player player = roomPlayer.getPlayer();
            ClothEquipment clothEquipment = roomPlayer.getClothEquipment();
            StatusPointsAddedDto statusPointsAddedDto = roomPlayer.getStatusPointsAddedDto();

            this.write(roomPlayer.getPosition());
            this.write(player.getName());
            this.write(player.getLevel());
            this.write(player.getAccount().getGameMaster());
            this.write(roomPlayer.isMaster());
            this.write(roomPlayer.isReady());
            this.write(roomPlayer.isFitting());
            this.write(player.getPlayerType());
            this.write(false);
            this.write((byte) 0); // unk3
            this.write(guild != null ? guild.getName() : "");

            if (guild != null) {
                this.write(guild.getLogoBackgroundId());
                this.write(guild.getLogoBackgroundColor());
                this.write(guild.getLogoPatternId());
                this.write(guild.getLogoPatternColor());
                this.write(guild.getLogoMarkId());
                this.write(guild.getLogoMarkColor());
            } else {
                for (int i = 0; i < 6; i++)
                    this.write(0);
            }

            this.write((byte) 0);
            this.write(roomPlayer.getCoupleId() != null ? roomPlayer.getCouple().getFriend().getName() : "");
            this.write(0);
            this.write((byte) 0);
            this.write((short) 0); // emblem slot 1
            this.write((short) 0); // emblem slot 2
            this.write((short) 0); // emblem slot 3
            this.write((short) 0); // emblem slot 4

            this.write(BattleUtils.calculatePlayerHp(player.getLevel()));

            // status points
            this.write(player.getStrength());
            this.write(player.getStamina());
            this.write(player.getDexterity());
            this.write(player.getWillpower());
            // enchant added status points
            this.write(statusPointsAddedDto.getAddStr());
            this.write(statusPointsAddedDto.getAddSta());
            this.write(statusPointsAddedDto.getAddDex());
            this.write(statusPointsAddedDto.getAddWil());
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
            // cloth added status points
            this.write(statusPointsAddedDto.getAddHp());
            this.write(statusPointsAddedDto.getStrength());
            this.write(statusPointsAddedDto.getStamina());
            this.write(statusPointsAddedDto.getDexterity());
            this.write(statusPointsAddedDto.getWillpower());
            // ??
            for (int i = 5; i < 13; ++i) {
                this.write((byte) 0);
            }
            // ??
            for (int i = 5; i < 13; ++i) {
                this.write((byte) 0);
            }
            /* end - status points */

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
