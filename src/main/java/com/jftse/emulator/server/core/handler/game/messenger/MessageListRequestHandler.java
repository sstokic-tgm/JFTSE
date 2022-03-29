package com.jftse.emulator.server.core.handler.game.messenger;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.messenger.C2SMessageListRequestPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CMessageListAnswerPacket;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.core.service.messenger.GiftService;
import com.jftse.emulator.server.core.service.messenger.MessageService;
import com.jftse.emulator.server.database.model.messenger.Gift;
import com.jftse.emulator.server.database.model.messenger.Message;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class MessageListRequestHandler extends AbstractHandler {
    private C2SMessageListRequestPacket messageListRequestPacket;

    private final PlayerService playerService;
    private final MessageService messageService;
    private final GiftService giftService;

    public MessageListRequestHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        messageService = ServiceManager.getInstance().getMessageService();
        giftService = ServiceManager.getInstance().getGiftService();
    }

    @Override
    public boolean process(Packet packet) {
        messageListRequestPacket = new C2SMessageListRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null)
            return;

        byte listType = messageListRequestPacket.getListType();

        Player player = playerService.findById(connection.getClient().getActivePlayer().getId());

        switch (listType) {
            case 0 -> {
                List<Message> messages = messageService.findByReceiver(player);
                S2CMessageListAnswerPacket messageListAnswerPacket = new S2CMessageListAnswerPacket(listType, messages);
                connection.sendTCP(messageListAnswerPacket);

                messages = new ArrayList<>(messageService.findBySender(player));
                messageListAnswerPacket = new S2CMessageListAnswerPacket((byte) (listType + 1), messages);
                connection.sendTCP(messageListAnswerPacket);
            }
            case 2 -> {
                List<Gift> gifts = giftService.findByReceiver(player);
                S2CMessageListAnswerPacket messageListAnswerPacket = new S2CMessageListAnswerPacket(listType, gifts);
                connection.sendTCP(messageListAnswerPacket);

                gifts = giftService.findBySender(player);
                messageListAnswerPacket = new S2CMessageListAnswerPacket((byte) (listType + 1), gifts);
                connection.sendTCP(messageListAnswerPacket);
            }
        }
    }
}
