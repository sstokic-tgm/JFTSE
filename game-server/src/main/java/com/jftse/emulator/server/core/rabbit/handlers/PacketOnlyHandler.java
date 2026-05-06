package com.jftse.emulator.server.core.rabbit.handlers;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.rabbit.AbstractMessageHandler;
import com.jftse.server.core.rabbit.MessageHandlerRegistry;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class PacketOnlyHandler extends AbstractMessageHandler<PacketMessage> {
    @Autowired
    private GameManager gameManager;

    @Override
    public void register(MessageHandlerRegistry registry) {
        registry.register("PACKET_ONLY", this);
    }

    @Override
    public void handle(PacketMessage message) {
        final FTConnection connection = gameManager.getConnectionByPlayerId(message.getReceivingPlayerId());
        if (connection != null) {
            connection.sendTCP(new Packet(message.getPacket()));
            log.info("Sent packet to player {}: {}", message.getReceivingPlayerId(), String.format("0x%X(%s)", message.getPacketId(), PacketOperations.getNameByValue(message.getPacketId())));
        }
    }
}
