package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class S2CClubMembersListAnswerPacket extends Packet {
    public S2CClubMembersListAnswerPacket(List<GuildMember> guildMembers) {
        super(PacketOperations.S2CClubMembersListAnswer);

        this.write((byte) guildMembers.size());
        for (GuildMember guildMember : guildMembers) {
            this.write(guildMember.getPlayer().getId().intValue());
            this.write(guildMember.getPlayer().getName());
            this.write(guildMember.getPlayer().getPlayerType());

            if (!guildMember.getPlayer().getOnline()) {
                this.write((short) -1);
            } else {
                Account account = ServiceManager.getInstance().getAuthenticationService().findAccountById(guildMember.getPlayer().getAccount().getId());
                if (account == null) {
                    this.write((short) -1);
                } else {
                    this.write((short) account.getLoggedInServer().getValue());
                }
            }
        }
    }
}
