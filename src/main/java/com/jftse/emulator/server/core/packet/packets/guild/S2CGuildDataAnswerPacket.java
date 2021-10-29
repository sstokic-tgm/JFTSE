package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.database.model.guild.Guild;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;
import java.util.stream.Collectors;

public class S2CGuildDataAnswerPacket extends Packet {
    public S2CGuildDataAnswerPacket(short guildStatus, Guild guild) {
        super(PacketID.S2CGuildDataAnswer);

        if (guildStatus == -2) {
            this.write(guildStatus);
        } else if (guildStatus == -1) {
            this.write(guildStatus);
            this.write(guild.getName());
            this.write(String.valueOf(guild.getLevel()));
        } else {
            this.write((short) 0);
            this.write(guild.getId().intValue());
            this.write(guild.getLogoBackgroundId());
            this.write(guild.getLogoBackgroundColor());
            this.write(guild.getLogoPatternId());
            this.write(guild.getLogoPatternColor());
            this.write(guild.getLogoMarkId());
            this.write(guild.getLogoMarkColor());
            this.write(guild.getName());

            List<GuildMember> memberList = guild.getMemberList();

            GuildMember clubMaster = memberList.stream().filter(gm -> gm.getMemberRank() == 3)
                    .findFirst().orElse(null);
            this.write(clubMaster.getPlayer().getName());

            List<GuildMember> subMasterList =
                    (List<GuildMember>) memberList.stream().filter(gm -> gm.getMemberRank() == 2)
                            .collect(Collectors.toList());
            this.write((byte) subMasterList.size());
            for (GuildMember subMaster : subMasterList)
                this.write(subMaster.getPlayer().getName());

            this.write((byte) memberList.stream().filter(x -> !x.getWaitingForApproval()).count());
            this.write(guild.getMaxMemberCount());

            List<GuildMember> reverseMemberList =
                    (List<GuildMember>) memberList.stream().filter(GuildMember::getWaitingForApproval)
                            .collect(Collectors.toList());
            this.write((byte) reverseMemberList.size());

            this.write(guild.getLevel());
            this.write(guild.getClubPoints());
            this.write(guild.getLeaguePoints());
            this.write(guild.getGold());
            this.write(guild.getBattleRecordWin());
            this.write(guild.getBattleRecordLoose());
            this.write(guild.getLeagueRecordWin());
            this.write(guild.getLeagueRecordLoose());
            this.write(guild.getLevelRestriction());
            this.write((byte) guild.getAllowedCharacterType().length);
            for (byte allowedCharacter : guild.getAllowedCharacterType())
                this.write(allowedCharacter);
            this.write(guild.getIsPublic());
            this.write(guild.getIntroduction());
            this.write(guild.getNotice());
            this.write(guild.getCreated());
            this.write(guild.isCastleOwner());
        }
    }
}
