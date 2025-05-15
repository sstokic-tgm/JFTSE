package com.jftse.emulator.server.core.rabbit.handlers;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packets.chat.S2CWhisperAnswerPacket;
import com.jftse.emulator.server.core.rabbit.MessageTypes;
import com.jftse.emulator.server.core.rabbit.messages.ChatWhisperMessage;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.rabbit.AbstractMessageHandler;
import com.jftse.server.core.rabbit.MessageHandlerRegistry;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ChatWhisperHandler extends AbstractMessageHandler<ChatWhisperMessage> {
    @Autowired
    private GameManager gameManager;
    @Autowired
    private RProducerService rProducerService;
    @Autowired
    private PlayerService playerService;

    @Override
    public void register(MessageHandlerRegistry registry) {
        registry.register(MessageTypes.CHAT_WHISPER.getValue(), this);
    }

    @Override
    public void handle(ChatWhisperMessage message) {
        final Long senderId = message.getSenderId();
        final Long receiverId = message.getReceiverId();
        final String chatMessage = message.getMessage();

        final FTConnection senderConnection = gameManager.getConnectionByPlayerId(senderId);
        final FTConnection receiverConnection = gameManager.getConnectionByPlayerId(receiverId);

        if (senderConnection != null && receiverConnection != null) {
            try {
                handleLocalWhisper(senderConnection, receiverConnection, chatMessage);
            } catch (ValidationException e) {
                handleOops(senderId, receiverId);
            }
        }

        if (senderConnection != null && receiverConnection == null) {
            handleOutgoingWhisper(message, senderConnection);
        }

        if (senderConnection == null && receiverConnection != null) {
            try {
                handleIncomingWhisper(senderId, receiverConnection, chatMessage);
            } catch (ValidationException e) {
                handleOops(senderId, receiverId);
            }
        }

        if (senderConnection == null && receiverConnection == null) {
            handleNotOnline(senderId);
        }
    }

    /**
     * Handles whisper message on the same server.
     *
     * @param senderConn   The connection of the sender
     * @param receiverConn The connection of the receiver
     * @param message      The whisper message to be sent
     */
    private void handleLocalWhisper(final FTConnection senderConn, final FTConnection receiverConn, String message) throws ValidationException {
        final FTClient senderClient = senderConn.getClient();
        final FTClient receiverClient = receiverConn.getClient();

        if (senderClient != null && receiverClient != null) {
            S2CWhisperAnswerPacket whisperPacket = new S2CWhisperAnswerPacket(senderClient.getPlayer().getName(), receiverClient.getPlayer().getName(), message);
            senderConn.sendTCP(whisperPacket);
            receiverConn.sendTCP(whisperPacket);
        } else {
            throw new ValidationException("Something went wrong.");
        }
    }

    /**
     * Cross-server whisper message handling. Send message to the game queue only to avoid looping of the handler.
     *
     * @param message    The message to be sent
     * @param senderConn The connection of the sender
     */
    private void handleOutgoingWhisper(ChatWhisperMessage message, final FTConnection senderConn) {
        rProducerService.send(message, "game.messenger.whisper", senderConn.getClient().getPlayer().getName() + "(ChatServer)");
    }

    /**
     * Cross-server whisper message handling. Here we send the message to the receiving player.
     *
     * @param senderId The player ID of the sender
     * @param conn     The connection of the receiver
     * @param message  The message to be sent
     */
    private void handleIncomingWhisper(Long senderId, final FTConnection conn, String message) throws ValidationException {
        final FTClient client = conn.getClient();
        if (client != null) {
            Player sendingPlayer = playerService.findById(senderId);
            S2CWhisperAnswerPacket whisperPacket = new S2CWhisperAnswerPacket(sendingPlayer.getName(), client.getPlayer().getName(), message);
            conn.sendTCP(whisperPacket);

            PacketMessage packetMessage = PacketMessage.builder()
                    .packet(whisperPacket)
                    .receivingPlayerId(senderId)
                    .build();
            rProducerService.send(packetMessage, "game.messenger.whisper chat.messenger.whisper", sendingPlayer.getName() + "(ChatServer)");
        } else {
            throw new ValidationException("Something went wrong.");
        }
    }

    /**
     * Handles the case when both sender and receiver are offline.
     *
     * @param senderId The player ID of the sender
     */
    private void handleNotOnline(Long senderId) {
        S2CChatLobbyAnswerPacket chatLobbyPacket = new S2CChatLobbyAnswerPacket((char) 0, "Whisper", "This user not connected.");
        Player sendingPlayer = playerService.findById(senderId);
        PacketMessage packetMessage = PacketMessage.builder()
                .packet(chatLobbyPacket)
                .receivingPlayerId(sendingPlayer.getId())
                .build();
        rProducerService.send(packetMessage, "game.messenger.whisper chat.messenger.whisper", sendingPlayer.getName() + "(ChatServer)");
    }

    private void handleOops(Long senderId, Long receiverId) {
        Player sendingPlayer = playerService.findById(senderId);
        Player receivingPlayer = playerService.findById(receiverId);
        S2CChatLobbyAnswerPacket chatLobbyPacket = new S2CChatLobbyAnswerPacket((char) 0, "Whisper", "Something went wrong.");
        PacketMessage packetMessage = PacketMessage.builder()
                .packet(chatLobbyPacket)
                .receivingPlayerId(senderId)
                .build();
        rProducerService.send(packetMessage, "game.messenger.whisper chat.messenger.whisper", sendingPlayer.getName() + "(ChatServer)");

        log.warn("Player {} not found in the system.", receivingPlayer.getName());
    }
}
