package com.jftse.emulator.server.core.handler.chat;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.rabbit.messages.ChatWhisperMessage;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.chat.CMSGChatMessageWhisper;
import com.jftse.server.core.shared.packets.chat.SMSGChatMessageLobby;

@PacketId(CMSGChatMessageWhisper.PACKET_ID)
public class ChatMessageWhisperPacketHandler implements PacketHandler<FTConnection, CMSGChatMessageWhisper> {
    @Override
    public void handle(FTConnection connection, CMSGChatMessageWhisper whisperReqPacket) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;
        FTPlayer player = client.getPlayer();

        Player receiver = ServiceManager.getInstance().getPlayerService().findByName(whisperReqPacket.getReceiver());
        if (receiver == null) {
            SMSGChatMessageLobby chatLobbyMessage = SMSGChatMessageLobby.builder()
                    .unk((char) 0)
                    .sender("Whisper")
                    .message("This user does not exist.")
                    .build();
            connection.sendTCP(chatLobbyMessage);
            return;
        }

        ChatWhisperMessage message = ChatWhisperMessage.builder()
                .senderId(player.getId())
                .receiverId(receiver.getId())
                .message(whisperReqPacket.getMessage())
                .build();
        RProducerService.getInstance().send(message, "game.messenger.whisper", player.getName() + "(GameServer)");
    }
}
