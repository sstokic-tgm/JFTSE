package com.jftse.emulator.server.core.handler.game.messenger;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.messenger.C2SSendMessageRequestPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CReceivedMessageNotificationPacket;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.core.service.messenger.MessageService;
import com.jftse.emulator.server.database.model.messenger.Message;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

public class SendMessageRequestHandler extends AbstractHandler {
    private C2SSendMessageRequestPacket c2SSendMessageRequestPacket;

    private final PlayerService playerService;
    private final MessageService messageService;

    public SendMessageRequestHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        messageService = ServiceManager.getInstance().getMessageService();
    }

    @Override
    public boolean process(Packet packet) {
        c2SSendMessageRequestPacket = new C2SSendMessageRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null)
            return;

        Player receiver = playerService.findByName(c2SSendMessageRequestPacket.getReceiverName());
        if (receiver != null) {
            Message message = new Message();
            message.setReceiver(receiver);
            message.setSender(connection.getClient().getActivePlayer());
            message.setMessage(c2SSendMessageRequestPacket.getMessage());
            message.setSeen(false);
            messageService.save(message);

            Client receiverClient = GameManager.getInstance().getClients().stream()
                    .filter(x -> x.getActivePlayer() != null && x.getActivePlayer().getId().equals(receiver.getId()))
                    .findFirst()
                    .orElse(null);
            if (receiverClient != null) {
                S2CReceivedMessageNotificationPacket s2CReceivedMessageNotificationPacket = new S2CReceivedMessageNotificationPacket(message);
                connection.getServer().sendToTcp(receiverClient.getConnection().getId(), s2CReceivedMessageNotificationPacket);
            }
        }
    }
}
