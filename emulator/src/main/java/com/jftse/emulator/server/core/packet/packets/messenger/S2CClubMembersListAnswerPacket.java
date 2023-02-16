package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class S2CClubMembersListAnswerPacket extends Packet {
    public S2CClubMembersListAnswerPacket(List<GuildMember> guildMembers) {
        super(PacketOperations.S2CClubMembersListAnswer.getValueAsChar());

        this.write((byte) guildMembers.size());
        for (GuildMember guildMember : guildMembers) {
            this.write(guildMember.getPlayer().getId().intValue());
            this.write(guildMember.getPlayer().getName());
            this.write(guildMember.getPlayer().getPlayerType());
            this.write(guildMember.getPlayer().getOnline() ? (short) 0 : (short) -1); // -1 offline, 0 online, 1...n game server id => game server id indicator
        }
    }
}
