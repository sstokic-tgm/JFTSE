package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.account.Account;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.Friend;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class S2CFriendsListAnswerPacket extends Packet {
    public S2CFriendsListAnswerPacket(List<Friend> friends) {
        super(PacketOperations.S2CFriendsListAnswer);

        this.write((byte) friends.size());
        for (Friend friend : friends) {
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
}
