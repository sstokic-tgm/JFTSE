package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.entities.database.model.messenger.AbstractMessage;
import com.jftse.entities.database.model.messenger.Gift;
import com.jftse.entities.database.model.messenger.Message;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CMessageListAnswerPacket extends Packet {
    public S2CMessageListAnswerPacket(byte listType, List<? extends AbstractMessage> messageList) {
        super(PacketOperations.S2CMessageListAnswer.getValueAsChar());

        this.write(listType);

        this.write((byte) messageList.size());
        for (AbstractMessage am : messageList) {
            if (am instanceof Message) {
                Message m = (Message) am;
                this.write(Math.toIntExact(m.getId()));
                this.write((listType % 2) == 0 ? m.getSender().getName() : m.getReceiver().getName());
                this.write((listType % 2) == 0 ? m.getSeen() : true);
                this.write(m.getMessage());
                this.write(m.getCreated());
                this.write(0); // product index
                this.write(m.getUseTypeOption());

            } else if (am instanceof Gift) {
                Gift g = (Gift) am;
                this.write(Math.toIntExact(g.getId()));
                this.write((listType % 2) == 0 ? g.getSender().getName() : g.getReceiver().getName());
                this.write((listType % 2) == 0 ? g.getSeen() : true);
                this.write(g.getMessage());
                this.write(g.getCreated());
                this.write(g.getProduct().getProductIndex());
                this.write(g.getUseTypeOption());
            }
        }
    }
}
