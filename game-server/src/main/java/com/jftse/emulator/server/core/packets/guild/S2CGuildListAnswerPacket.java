package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class S2CGuildListAnswerPacket extends Packet {
    public S2CGuildListAnswerPacket(List<Guild> guildList) {
        super(PacketOperations.S2CGuildListAnswer.getValue());

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
            if (clubMaster == null)
                log.debug("clubMaster == null, guild id: " + guild.getId() + " guild name: " + guild.getName());
            this.write(clubMaster.getPlayer().getName());
            this.write(guild.getLevel());
            this.write(guild.getLevelRestriction());
            this.write((byte)memberList.stream().filter(x -> !x.getWaitingForApproval()).count());
            this.write(guild.getMaxMemberCount());
            this.write(guild.getBattleRecordWin());
            this.write(guild.getBattleRecordLoose());
            this.write(guild.getLeagueRecordWin());
            this.write(guild.getLeagueRecordLoose());
            this.write(guild.getIntroduction());
            this.write(guild.getCreated());
            this.write(guild.getCastleOwner());
            this.write((byte)guild.getAllowedCharacterType().length);
            for (byte allowedCharacter : guild.getAllowedCharacterType())
                this.write(allowedCharacter);
        }
    }
}
