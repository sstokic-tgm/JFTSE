package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomUserInfoAnswerPacket extends Packet {
    public S2CRoomUserInfoAnswerPacket(char result, RoomPlayer roomPlayer) {
        super(PacketOperations.S2CRoomUserInfoAnswer);

        this.write(result);

        if (result == 0) {
            Guild guild = null;
            GuildMember guildMember = roomPlayer.getGuildMember();
            if (guildMember != null && !guildMember.getWaitingForApproval() && guildMember.getGuild() != null)
                guild = guildMember.getGuild();

            Player player = roomPlayer.getPlayer();
            Friend couple = roomPlayer.getCouple();
            PlayerStatistic playerStatistic = roomPlayer.getPlayerStatistic();

            this.write(roomPlayer.getPosition());
            this.write(player.getName());

            this.write(playerStatistic.getBasicRecordWin());
            this.write(playerStatistic.getBasicRecordLoss());
            this.write(playerStatistic.getBattleRecordWin());
            this.write(playerStatistic.getBattleRecordLoss());
            this.write(playerStatistic.getConsecutiveWins());
            this.write(0);
            this.write(playerStatistic.getNumberOfDisconnects());
            this.write(playerStatistic.getTotalGames());
            this.write(0);
            this.write(0);

            this.write(0); // perfect(s)
            this.write(0); // guard break(s)
            this.write(guild != null ? guild.getName() : ""); // guild name

            // ?? 2x player info play stats data with a boolean true which adds 3 additional int to be written otherwise there would be 20 ints only
            for (int i = 0; i < 26; i++)
                this.write(0);
        }
    }
}
