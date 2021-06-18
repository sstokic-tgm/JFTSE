package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.database.model.messaging.Friend;
import com.jftse.emulator.server.database.model.messaging.Message;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class S2CReceivedMessageNotificationPacket extends Packet {
    public S2CReceivedMessageNotificationPacket(Message message) {
        super(PacketID.S2CReceivedMessageNotification);

        this.write(message.getId().intValue());
        this.write(message.getSender().getName());
        this.write(message.getSeen());
        this.write(message.getMessage());
        this.write(message.getCreated());
    }
}
