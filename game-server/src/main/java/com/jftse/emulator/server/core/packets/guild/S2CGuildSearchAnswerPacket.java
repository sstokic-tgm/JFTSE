package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;

import java.util.List;

public class S2CGuildSearchAnswerPacket extends Packet {
    public S2CGuildSearchAnswerPacket(List<Guild> guildList) {
        super(PacketOperations.S2CGuildSearchAnswer.getValue());

        this.write((byte)guildList.size());
        for (Guild guild : guildList) {
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
            this.write((byte) memberList.size());
            this.write(guild.getMaxMemberCount());
            this.write(guild.getBattleRecordWin());
            this.write(guild.getBattleRecordLoose());
            this.write(guild.getLeagueRecordWin());
            this.write(guild.getLeagueRecordLoose());
            this.write(guild.getIntroduction());
            this.write(guild.getCreated());
            this.write((byte) guild.getAllowedCharacterType().length);
            for (byte allowedCharacter : guild.getAllowedCharacterType())
                this.write(allowedCharacter);
        }
    }
}
