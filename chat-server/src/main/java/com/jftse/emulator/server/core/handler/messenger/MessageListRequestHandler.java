package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.packets.messenger.C2SMessageListRequestPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CMessageListAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.messenger.AbstractMessage;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.Gift;
import com.jftse.entities.database.model.messenger.Message;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.GiftService;
import com.jftse.server.core.service.MessageService;

import java.util.ArrayList;
import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SMessageListRequest)
public class MessageListRequestHandler extends AbstractPacketHandler {
    private C2SMessageListRequestPacket messageListRequestPacket;

    private final MessageService messageService;
    private final GiftService giftService;

    public MessageListRequestHandler() {
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
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        byte listType = messageListRequestPacket.getListType();

        Player player = ftClient.getPlayer();

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

                boolean allSeen = gifts.stream().allMatch(Gift::getSeen);
                if (!allSeen) {
                    Packet packet = new Packet(PacketOperations.S2CYouGotPresentMessage);
                    packet.write((byte) 17);
                    connection.sendTCP(packet);
                }

                gifts = giftService.findBySender(player);
                messageListAnswerPacket = new S2CMessageListAnswerPacket((byte) (listType + 1), gifts);
                connection.sendTCP(messageListAnswerPacket);
            }
        }
    }
}
