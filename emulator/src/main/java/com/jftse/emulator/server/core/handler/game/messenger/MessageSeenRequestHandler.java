package com.jftse.emulator.server.core.handler.game.messenger;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.messenger.C2SMessageSeenRequestPacket;
import com.jftse.emulator.server.core.service.messenger.GiftService;
import com.jftse.emulator.server.core.service.messenger.MessageService;
import com.jftse.entities.database.model.messenger.Gift;
import com.jftse.entities.database.model.messenger.Message;
import com.jftse.emulator.server.networking.packet.Packet;

public class MessageSeenRequestHandler extends AbstractHandler {
    private C2SMessageSeenRequestPacket c2SMessageSeenRequestPacket;

    private final MessageService messageService;
    private final GiftService giftService;

    public MessageSeenRequestHandler() {
        messageService = ServiceManager.getInstance().getMessageService();
        giftService = ServiceManager.getInstance().getGiftService();
    }

    @Override
    public boolean process(Packet packet) {
        c2SMessageSeenRequestPacket = new C2SMessageSeenRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (c2SMessageSeenRequestPacket.getType() == 0) {
            Message message = messageService.findById(c2SMessageSeenRequestPacket.getMessageId().longValue());
            message.setSeen(true);
            messageService.save(message);
        } else if (c2SMessageSeenRequestPacket.getType() == 2) {
            Gift gift = giftService.findById(c2SMessageSeenRequestPacket.getMessageId().longValue());
            gift.setSeen(true);
            giftService.save(gift);
        }
    }
}
