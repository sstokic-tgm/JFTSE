package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CRelationshipAnswerPacket extends Packet {
    public S2CRelationshipAnswerPacket(Friend friend) {
        super(PacketOperations.S2CRelationshipAnswer);
        this.write(friend.getFriend().getId().intValue());
        this.write(friend.getFriend().getName());
        this.write(friend.getFriend().getPlayerType());

        if (!friend.getFriend().getOnline()) {
            this.write((short) -1);
        } else {
            Account account = ServiceManager.getInstance().getAuthenticationService().findAccountById(friend.getFriend().getAccount().getId());
            if (account == null) {
                this.write((short) -1);
            } else {
                this.write((short) account.getLoggedInServer().getValue());
            }
        }
    }
}
