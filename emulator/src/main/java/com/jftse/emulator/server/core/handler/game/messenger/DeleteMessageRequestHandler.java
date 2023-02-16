package com.jftse.emulator.server.core.handler.game.messenger;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.messenger.C2SDeleteMessagesRequest;
import com.jftse.emulator.server.core.service.messenger.GiftService;
import com.jftse.emulator.server.core.service.messenger.MessageService;
import com.jftse.emulator.server.networking.packet.Packet;

public class DeleteMessageRequestHandler extends AbstractHandler {
    private C2SDeleteMessagesRequest c2SDeleteMessagesRequest;

    private final MessageService messageService;
    private final GiftService giftService;

    public DeleteMessageRequestHandler() {
        messageService = ServiceManager.getInstance().getMessageService();
        giftService = ServiceManager.getInstance().getGiftService();
    }

    @Override
    public boolean process(Packet packet) {
        c2SDeleteMessagesRequest = new C2SDeleteMessagesRequest(packet);
        return true;
    }

    @Override
    public void handle() {
        if (c2SDeleteMessagesRequest.getType() == 0) {
            c2SDeleteMessagesRequest.getMessageIds().forEach(m -> messageService.remove(m.longValue()));
        } else if (c2SDeleteMessagesRequest.getType() == 2) {
            c2SDeleteMessagesRequest.getMessageIds().forEach(m -> giftService.remove(m.longValue()));
        }
    }
}
