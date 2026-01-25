package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.messenger.S2CReceivedMessageNotificationPacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.messenger.Message;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.MessageService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.shared.packets.messenger.CMSGSendMessage;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;

import java.util.List;

@PacketId(CMSGSendMessage.PACKET_ID)
public class SendMessageRequestHandler implements PacketHandler<FTConnection, CMSGSendMessage> {
    private final PlayerService playerService;
    private final MessageService messageService;

    private final RProducerService rProducerService;

    public SendMessageRequestHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        messageService = ServiceManager.getInstance().getMessageService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGSendMessage packet) {
        FTClient ftClient = connection.getClient();
        if (!ftClient.hasPlayer())
            return;

        FTPlayer player = ftClient.getPlayer();
        Player receiver = playerService.findByName(packet.getReceiverName());
        if (receiver != null) {
            List<Message> messages = messageService.findByReceiver(receiver);
            List<Message> senderMessages = messageService.findBySender(player.getPlayerRef());
            if (messages.size() > 128 || senderMessages.size() > 128) {
                return;
            }

            Message message = new Message();
            message.setReceiver(receiver);
            message.setSender(player.getPlayerRef());
            message.setMessage(packet.getMessage());
            message.setSeen(false);
            messageService.save(message);

            S2CReceivedMessageNotificationPacket s2CReceivedMessageNotificationPacket = new S2CReceivedMessageNotificationPacket(message, player.getName());

            PacketMessage packetMessage = PacketMessage.builder()
                    .receivingPlayerId(receiver.getId())
                    .packet(s2CReceivedMessageNotificationPacket)
                    .build();
            rProducerService.send(packetMessage, "game.messenger.message chat.messenger.message", player.getName() + "(GameServer)");
        }
    }
}
