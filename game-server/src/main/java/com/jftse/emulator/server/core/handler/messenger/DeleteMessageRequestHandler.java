package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GiftService;
import com.jftse.server.core.service.MessageService;
import com.jftse.server.core.shared.packets.messenger.CMSGDeleteMessage;

@PacketId(CMSGDeleteMessage.PACKET_ID)
public class DeleteMessageRequestHandler implements PacketHandler<FTConnection, CMSGDeleteMessage> {
    private final MessageService messageService;
    private final GiftService giftService;

    public DeleteMessageRequestHandler() {
        messageService = ServiceManager.getInstance().getMessageService();
        giftService = ServiceManager.getInstance().getGiftService();
    }

    @Override
    public void handle(FTConnection connection, CMSGDeleteMessage packet) {
        if (packet.getType() == 0) {
            packet.getMessageIds().forEach(m -> messageService.remove(m.longValue()));
        } else if (packet.getType() == 2) {
            packet.getMessageIds().forEach(m -> giftService.remove(m.longValue()));
        }
    }
}
