package com.jftse.emulator.server.core.handler.chat;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.chat.C2SWhisperReqPacket;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.rabbit.messages.ChatWhisperMessage;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SWhisperReq)
public class ChatMessageWhisperPacketHandler extends AbstractPacketHandler {
    private C2SWhisperReqPacket whisperReqPacket;

    @Override
    public boolean process(Packet packet) {
        whisperReqPacket = new C2SWhisperReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        Player player = client.getPlayer();
        if (player == null) {
            return;
        }

        Player receiver = ServiceManager.getInstance().getPlayerService().findByName(whisperReqPacket.getReceiverName());
        if (receiver == null) {
            S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "Whisper", "This user does not exist.");
            connection.sendTCP(chatLobbyAnswerPacket);
            return;
        }

        ChatWhisperMessage message = ChatWhisperMessage.builder()
                .senderId(player.getId())
                .receiverId(receiver.getId())
                .message(whisperReqPacket.getMessage())
                .build();
        RProducerService.getInstance().send(message, "chat.messenger.whisper", player.getName() + "(ChatServer)");
    }
}
