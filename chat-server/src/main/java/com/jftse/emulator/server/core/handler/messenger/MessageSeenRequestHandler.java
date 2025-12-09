package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.net.FTConnection;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.entities.database.model.messenger.Gift;
import com.jftse.entities.database.model.messenger.Message;
import com.jftse.server.core.service.GiftService;
import com.jftse.server.core.service.MessageService;
import com.jftse.server.core.shared.packets.messenger.CMSGSeeMessage;

@PacketId(CMSGSeeMessage.PACKET_ID)
public class MessageSeenRequestHandler implements PacketHandler<FTConnection, CMSGSeeMessage> {
    private final MessageService messageService;
    private final GiftService giftService;

    public MessageSeenRequestHandler() {
        messageService = ServiceManager.getInstance().getMessageService();
        giftService = ServiceManager.getInstance().getGiftService();
    }

    @Override
    public void handle(FTConnection connection, CMSGSeeMessage packet) {
        if (packet.getType() == 0) {
            Message message = messageService.findById((long) packet.getMessageId());
            message.setSeen(true);
            messageService.save(message);
        } else if (packet.getType() == 2) {
            Gift gift = giftService.findById((long) packet.getMessageId());
            gift.setSeen(true);
            giftService.save(gift);
        }
    }
}
