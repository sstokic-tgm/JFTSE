package com.jftse.emulator.server.core.rabbit.handlers;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.server.core.rabbit.AbstractMessageHandler;
import com.jftse.server.core.rabbit.MessageHandlerRegistry;
import com.jftse.server.core.shared.packets.S2CServerNoticePacket;
import com.jftse.server.core.shared.rabbit.messages.ServerNoticeMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ServerNoticeHandler extends AbstractMessageHandler<ServerNoticeMessage> {
    @Autowired
    private GameManager gameManager;

    @Override
    public void register(MessageHandlerRegistry registry) {
        registry.register("SERVER_NOTICE", this);
    }

    @Override
    public void handle(ServerNoticeMessage message) {
        String msg = message.getMessage().equalsIgnoreCase("off") ? "" : message.getMessage();
        log.info("Sending server notice: {}", msg);

        gameManager.setMotd(msg);

        S2CServerNoticePacket serverNoticePacket = new S2CServerNoticePacket(msg);
        gameManager.getClients().forEach(client -> {
            if (client.getConnection() != null)
                client.getConnection().sendTCP(serverNoticePacket);
        });
    }
}
