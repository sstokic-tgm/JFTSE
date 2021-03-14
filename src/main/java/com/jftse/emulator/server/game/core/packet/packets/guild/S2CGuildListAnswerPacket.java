package com.jftse.emulator.server.game.core.packet.packets.guild;

import com.jftse.emulator.server.database.model.guild.Guild;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;
import java.util.stream.Collectors;

public class S2CGuildListAnswerPacket extends Packet {
    public S2CGuildListAnswerPacket(List<Guild> guildList) {
        super(PacketID.S2CGuildListAnswer);

        this.write((byte)guildList.size());
        for (int i = 0; i < guildList.size(); i++)
        {
            Guild guild = guildList.get(i);
            this.write(guild.getId().intValue());
            this.write(guild.getLogoBackgroundId());
            this.write(guild.getLogoBackgroundColor());
            this.write(guild.getLogoPatternId());
            this.write(guild.getLogoPatternColor());
            this.write(guild.getLogoMarkId());
            this.write(guild.getLogoMarkColor());
            this.write(guild.getName());
            this.write(guild.getIsPublic());
            List<GuildMember> memberList = guild.getMemberList();
            GuildMember clubMaster = memberList.stream().filter(gm -> gm.getMemberRank() == 3).findFirst().orElse(null);
            this.write(clubMaster.getPlayer().getName());
            this.write(guild.getLevel());
            this.write(guild.getLevelRestriction());
            this.write((byte)memberList.size());
            this.write(guild.getMaxMemberCount());
            this.write(guild.getBattleRecordWin());
            this.write(guild.getBattleRecordLoose());
            this.write(guild.getLeagueRecordWin());
            this.write(guild.getLeagueRecordLoose());
            this.write(guild.getIntroduction());
            this.write(guild.getCreated());
            this.write(guild.isCastleOwner());
            this.write((byte)guild.getAllowedCharacterType().length);
            for (byte allowedCharacter : guild.getAllowedCharacterType())
                this.write(allowedCharacter);
        }
    }
}
