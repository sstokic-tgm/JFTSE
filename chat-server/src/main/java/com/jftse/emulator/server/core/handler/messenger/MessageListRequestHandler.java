package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.messenger.S2CMessageListAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.messenger.Gift;
import com.jftse.entities.database.model.messenger.Message;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GiftService;
import com.jftse.server.core.service.MessageService;
import com.jftse.server.core.shared.packets.messenger.CMSGMessageList;
import com.jftse.server.core.shared.packets.messenger.SMSGReceivedPresent;

import java.util.ArrayList;
import java.util.List;

@PacketId(CMSGMessageList.PACKET_ID)
public class MessageListRequestHandler implements PacketHandler<FTConnection, CMSGMessageList> {
    private final MessageService messageService;
    private final GiftService giftService;

    public MessageListRequestHandler() {
        messageService = ServiceManager.getInstance().getMessageService();
        giftService = ServiceManager.getInstance().getGiftService();
    }

    @Override
    public void handle(FTConnection connection, CMSGMessageList packet) {
        FTClient ftClient = connection.getClient();
        if (!ftClient.hasPlayer())
            return;

        byte listType = packet.getListType();

        FTPlayer player = ftClient.getPlayer();

        switch (listType) {
            case 0 -> {
                List<Message> messages = messageService.findWithPlayerByReceiver(player.getId());
                S2CMessageListAnswerPacket messageListAnswerPacket = new S2CMessageListAnswerPacket(listType, messages);
                connection.sendTCP(messageListAnswerPacket);

                messages = new ArrayList<>(messageService.findWithPlayerBySender(player.getId()));
                messageListAnswerPacket = new S2CMessageListAnswerPacket((byte) (listType + 1), messages);
                connection.sendTCP(messageListAnswerPacket);
            }
            case 2 -> {
                List<Gift> gifts = giftService.findWithPlayerByReceiver(player.getId());
                S2CMessageListAnswerPacket messageListAnswerPacket = new S2CMessageListAnswerPacket(listType, gifts);
                connection.sendTCP(messageListAnswerPacket);

                boolean allSeen = gifts.stream().allMatch(Gift::getSeen);
                if (!allSeen) {
                    SMSGReceivedPresent response = SMSGReceivedPresent.builder().result((byte) 17).build();
                    connection.sendTCP(response);
                }

                gifts = giftService.findWithPlayerBySender(player.getId());
                messageListAnswerPacket = new S2CMessageListAnswerPacket((byte) (listType + 1), gifts);
                connection.sendTCP(messageListAnswerPacket);
            }
        }
    }
}
