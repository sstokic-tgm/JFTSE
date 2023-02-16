package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.Message;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CReceivedMessageNotificationPacket extends Packet {
    public S2CReceivedMessageNotificationPacket(Message message) {
        super(PacketOperations.S2CReceivedMessageNotification);

        this.write(message.getId().intValue());
        this.write(message.getSender().getName());
        this.write(message.getSeen());
        this.write(message.getMessage());
        this.write(message.getCreated());
    }
}
