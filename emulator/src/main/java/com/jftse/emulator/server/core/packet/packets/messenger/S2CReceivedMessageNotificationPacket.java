package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.entities.database.model.messenger.Message;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CReceivedMessageNotificationPacket extends Packet {
    public S2CReceivedMessageNotificationPacket(Message message) {
        super(PacketOperations.S2CReceivedMessageNotification.getValueAsChar());

        this.write(message.getId().intValue());
        this.write(message.getSender().getName());
        this.write(message.getSeen());
        this.write(message.getMessage());
        this.write(message.getCreated());
    }
}
