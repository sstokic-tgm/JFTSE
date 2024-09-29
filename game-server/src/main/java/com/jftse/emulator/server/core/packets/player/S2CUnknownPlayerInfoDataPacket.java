package com.jftse.emulator.server.core.packets.player;

import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.Map;

public class S2CUnknownPlayerInfoDataPacket extends Packet {
    public S2CUnknownPlayerInfoDataPacket(Player player, Pocket pocket, Map<String, Integer> equippedCloths, StatusPointsAddedDto statusPointsAddedDto, PlayerStatistic playerStatistic, Guild guild) {
        super(PacketOperations.S2CUnknownPlayerInfoData);

        this.write(player.getName());

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
        this.write(player.getStatusPoints());

        this.write(-1); // active pet type
        this.write(pocket.getMaxBelongings().shortValue());
        this.write((byte) 0); // card slots

        this.write(equippedCloths.get("hair"));
        this.write(equippedCloths.get("face"));
        this.write(equippedCloths.get("dress"));
        this.write(equippedCloths.get("pants"));
        this.write(equippedCloths.get("socks"));
        this.write(equippedCloths.get("shoes"));
        this.write(equippedCloths.get("gloves"));
        this.write(equippedCloths.get("racket"));
        this.write(equippedCloths.get("glasses"));
        this.write(equippedCloths.get("bag"));
        this.write(equippedCloths.get("hat"));
        this.write(equippedCloths.get("dye"));

        this.write(player.getCouplePoints());
        this.write(0); // ??
    }
}
