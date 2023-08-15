package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

import java.util.List;

public class S2CMatchplaySetExperienceGainInfoData extends Packet {
    public S2CMatchplaySetExperienceGainInfoData(byte resultTitle, int secondsNeeded, PlayerReward playerReward, byte playerLevel, RoomPlayer roomPlayer) {
        super(PacketOperations.S2CMatchPlaySetExperienceGainInfoData);

        this.write(resultTitle); // 0 = Loser, 1 = Winner
        this.write(playerLevel); // level

        this.write(playerReward != null ? playerReward.getExp() : 0); // EXP BASIC
        this.write(playerReward != null ? playerReward.getGold() : 0); // GOLD BASIC
        this.write(0); // EXP BONUS
        this.write(0); // GOLD BONUS
        this.write(playerReward != null ? playerReward.getExp() : 0); // EXP TOTAL -> current exp + won exp
        this.write(playerReward != null ? playerReward.getGold() : 0); // GOLD TOTAL -> current gold + won gold

        this.write((byte) 0); // perfects
        this.write((byte) 0); // guards

        this.write(secondsNeeded); // Playtime in seconds
        this.write(playerReward != null ? playerReward.getRankingPoints() : 0); // Ranking point reward
        this.write(0); // Unk
        this.write(0); // Unk

        // 0000 0001 = PF, 0000 0010 = GB, 0000 0100 = Time, 0000 1000 = matchplay, 0001 0000 = Lv up, ...
        // 0000 0001 = Couple Bonus
        // 0000 0001 = EXP Bonus, 0000 0010 = Gold Bonus, 0000 1000 = Ring Wiseman, 0000 0100 = Event
        this.write(playerReward != null ? playerReward.getActiveBonuses() : 0); // Bonus (1 = Perfect, ...)

        this.write((byte) 0);

        this.write(0);
        this.write(0);
        this.write(0);

        Player player = roomPlayer.getPlayer();
        StatusPointsAddedDto statusPointsAddedDto = roomPlayer.getStatusPointsAddedDto();
        List<Integer> specialSlotEquipment = roomPlayer.getSpecialSlotEquipment();
        List<Integer> cardSlotEquipment = roomPlayer.getCardSlotEquipment();

        specialSlotEquipment.forEach(this::write);
        cardSlotEquipment.forEach(this::write);

        this.write(BattleUtils.calculatePlayerHp(playerLevel));
        // status points
        this.write(player.getStrength());
        this.write(player.getStamina());
        this.write(player.getDexterity());
        this.write(player.getWillpower());
        // cloth added status points
        this.write(statusPointsAddedDto.getAddHp());
        this.write(statusPointsAddedDto.getStrength());
        this.write(statusPointsAddedDto.getStamina());
        this.write(statusPointsAddedDto.getDexterity());
        this.write(statusPointsAddedDto.getWillpower());
    }
}