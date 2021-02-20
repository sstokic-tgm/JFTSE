package com.jftse.emulator.server.game.core.packet.packets.lobby.room;

import com.jftse.emulator.server.database.model.player.ClothEquipment;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.game.core.utils.BattleUtils;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CRoomPlayerInformationPacket extends Packet {
    public S2CRoomPlayerInformationPacket(List<RoomPlayer> roomPlayerList) {
        super(PacketID.S2CRoomPlayerInformation);

        this.write((char) roomPlayerList.size());
        for (RoomPlayer roomPlayer : roomPlayerList) {
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
            this.write((byte) 0); // unk2
            this.write((byte) 0); // unk3
            this.write(""); // guild name
            this.write(0);
            this.write(0);
            this.write(0);
            this.write(0);
            this.write(0);
            this.write(0);
            this.write((byte) 0);
            this.write("");
            this.write(0);
            this.write((byte) 0);
            this.write((short) 0); // emblem slot 1
            this.write((short) 0); // emblem slot 2
            this.write((short) 0); // emblem slot 3
            this.write((short) 0); // emblem slot 4
            this.write(0);

            /* start - status points */
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
            this.write(BattleUtils.calculatePlayerHp(player));
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