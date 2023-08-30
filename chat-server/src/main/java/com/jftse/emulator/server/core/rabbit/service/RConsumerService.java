package com.jftse.emulator.server.core.rabbit.service;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.thread.ThreadManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
@Log4j2
public class RConsumerService {
    private final GameManager gameManager;
    private final RProducerService rProducerService;

    @Autowired
    public RConsumerService(GameManager gameManager, RProducerService rProducerService) {
        this.gameManager = gameManager;
        this.rProducerService = rProducerService;
    }

    @RabbitListener(queues = {"${jftse.rabbitmq.queue.game-to-chat}"})
    public void receiveMessage(Message message) {
        ThreadManager.getInstance().submit(() -> {
            MessageProperties messageProperties = message.getMessageProperties();
            Long playerId = messageProperties.getHeader("playerId");

            if (playerId != null) {
                Packet packet = new Packet(message.getBody());

                FTConnection connection = gameManager.getConnectionByPlayerId(playerId);
                if (connection != null) {
                    connection.sendTCP(packet);
                    handleSpecificPacketsOnClientFound(messageProperties, packet);
                } else {
                    log.warn("Client with playerId {} not online", playerId);

                    handleSpecificPacketsOnClientNotFound(messageProperties, packet);
                }
            } else {
                log.warn("playerId not found in message properties");
            }
        });
    }

    private void handleSpecificPacketsOnClientFound(MessageProperties messageProperties, Packet packet) {
        PacketOperations packetOperation = PacketOperations.getPacketOperationByValue(packet.getPacketId());
        if (packetOperation != null) {
            switch (packetOperation) {
                case S2CWhisperAnswer -> {
                    Long senderPlayerId = messageProperties.getHeader("senderPlayerId");
                    rProducerService.send("playerId", senderPlayerId, packet);
                }
            }
        } else {
            log.warn("Packet operation not found for packetId {}", packet.getPacketId());
        }
    }

    private void handleSpecificPacketsOnClientNotFound(MessageProperties messageProperties, Packet packet) {
        PacketOperations packetOperation = PacketOperations.getPacketOperationByValue(packet.getPacketId());
        if (packetOperation != null) {
            switch (packetOperation) {
                case S2CWhisperAnswer -> {
                    Long senderPlayerId = messageProperties.getHeader("senderPlayerId");
                    S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "Whisper", "This user not connected.");
                    rProducerService.send("playerId", senderPlayerId, chatLobbyAnswerPacket);
                }
            }
        } else {
            log.warn("Packet operation not found for packetId {}", packet.getPacketId());
        }
    }
}
