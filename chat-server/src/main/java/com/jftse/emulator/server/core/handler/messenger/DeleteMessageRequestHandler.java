package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.packets.messenger.C2SDeleteMessagesRequest;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.GiftService;
import com.jftse.server.core.service.MessageService;

@PacketOperationIdentifier(PacketOperations.C2SDeleteMessagesRequest)
public class DeleteMessageRequestHandler extends AbstractPacketHandler {
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
