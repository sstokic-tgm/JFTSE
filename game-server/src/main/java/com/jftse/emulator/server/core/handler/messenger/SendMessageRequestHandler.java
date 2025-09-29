package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.packets.messenger.C2SSendMessageRequestPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CReceivedMessageNotificationPacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.Message;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.MessageService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SSendMessageRequest)
public class SendMessageRequestHandler extends AbstractPacketHandler {
    private C2SSendMessageRequestPacket c2SSendMessageRequestPacket;

    private final PlayerService playerService;
    private final MessageService messageService;

    private final RProducerService rProducerService;

    public SendMessageRequestHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        messageService = ServiceManager.getInstance().getMessageService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public boolean process(Packet packet) {
        c2SSendMessageRequestPacket = new C2SSendMessageRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        Player receiver = playerService.findByName(c2SSendMessageRequestPacket.getReceiverName());
        if (receiver != null) {
            List<Message> messages = messageService.findByReceiver(receiver);
            List<Message> senderMessages = messageService.findBySender(ftClient.getPlayer());
            if (messages.size() > 128 || senderMessages.size() > 128) {
                return;
            }

            Message message = new Message();
            message.setReceiver(receiver);
            message.setSender(ftClient.getPlayer());
            message.setMessage(c2SSendMessageRequestPacket.getMessage());
            message.setSeen(false);
            messageService.save(message);

            S2CReceivedMessageNotificationPacket s2CReceivedMessageNotificationPacket = new S2CReceivedMessageNotificationPacket(message);

            PacketMessage packetMessage = PacketMessage.builder()
                    .receivingPlayerId(receiver.getId())
                    .packet(s2CReceivedMessageNotificationPacket)
                    .build();
            rProducerService.send(packetMessage, "game.messenger.message chat.messenger.message", ftClient.getPlayer().getName() + "(GameServer)");
        }
    }
}
