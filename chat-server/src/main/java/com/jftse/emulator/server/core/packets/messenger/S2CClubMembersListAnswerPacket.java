package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class S2CClubMembersListAnswerPacket extends Packet {
    public S2CClubMembersListAnswerPacket(List<Player> guildMembers) {
        super(PacketOperations.S2CClubMembersListAnswer);

        this.write((byte) guildMembers.size());
        for (Player guildMember : guildMembers) {
            this.write(guildMember.getId().intValue());
            this.write(guildMember.getName());
            this.write(guildMember.getPlayerType());

            if (!guildMember.getOnline()) {
                this.write((short) -1);
            } else {
                Account account = guildMember.getAccount();
                if (account == null || account.getLoggedInServer() == null) {
                    this.write((short) -1);
                } else {
                    this.write((short) account.getLoggedInServer().getValue());
                }
            }
        }
    }
}
