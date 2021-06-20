package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class S2CClubMembersListAnswerPacket extends Packet {
    public S2CClubMembersListAnswerPacket(List<GuildMember> guildMembers) {
        super(PacketID.S2CClubMembersListAnswer);

        this.write((byte) guildMembers.size());
        for (GuildMember guildMember : guildMembers) {
            this.write(guildMember.getPlayer().getId().intValue());
            this.write(guildMember.getPlayer().getName());
            this.write(guildMember.getPlayer().getPlayerType());
            this.write(guildMember.getPlayer().getOnline() ? (short) 0 : (short) -1);
        }
    }
}
