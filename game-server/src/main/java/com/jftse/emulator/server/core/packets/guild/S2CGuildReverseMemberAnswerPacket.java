package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.entities.database.model.guild.GuildMember;

import java.util.List;

public class S2CGuildReverseMemberAnswerPacket extends Packet {
    public S2CGuildReverseMemberAnswerPacket(List<GuildMember> memberList) {
        super(PacketOperations.S2CGuildReserveMemberDataAnswer);

        this.write((short)0);
        this.write((byte)memberList.size());

        for (GuildMember guildMember : memberList) {
            this.write(guildMember.getPlayer().getId().intValue());
            this.write(guildMember.getPlayer().getLevel());
            this.write(guildMember.getPlayer().getPlayerType());
            this.write(guildMember.getPlayer().getName());
            this.write(guildMember.getRequestDate());
        }
    }
}