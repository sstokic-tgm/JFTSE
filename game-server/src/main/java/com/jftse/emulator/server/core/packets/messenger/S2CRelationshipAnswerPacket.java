package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CRelationshipAnswerPacket extends Packet {
    public S2CRelationshipAnswerPacket(Player friend) {
        super(PacketOperations.S2CRelationshipAnswer);
        this.write(friend.getId().intValue());
        this.write(friend.getName());
        this.write(friend.getPlayerType());

        if (!friend.getOnline()) {
            this.write((short) -1);
        } else {
            Account account = friend.getAccount();
            if (account == null || account.getLoggedInServer() == null) {
                this.write((short) -1);
            } else {
                this.write((short) account.getLoggedInServer().getValue());
            }
        }
    }
}
