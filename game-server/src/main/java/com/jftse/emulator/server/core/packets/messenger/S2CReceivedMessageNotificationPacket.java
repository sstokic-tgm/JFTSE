package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.entities.database.model.messenger.Message;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CReceivedMessageNotificationPacket extends Packet {
    public S2CReceivedMessageNotificationPacket(Message message, String sender) {
        super(PacketOperations.S2CReceivedMessageNotification);

        this.write(message.getId().intValue());
        this.write(sender);
        this.write(message.getSeen());
        this.write(message.getMessage());
        this.write(message.getCreated());
    }
}
