package com.jftse.emulator.server.core.rabbit.service;

import com.jftse.emulator.server.rabbit.RabbitMQConfig;
import com.jftse.server.core.protocol.Packet;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class RProducerService {
    private static RProducerService instance;

    private final RabbitMQConfig rabbitMQConfig;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RProducerService(RabbitMQConfig rabbitMQConfig, RabbitTemplate rabbitTemplate) {
        instance = this;

        this.rabbitMQConfig = rabbitMQConfig;
        this.rabbitTemplate = rabbitTemplate;
    }

    public static RProducerService getInstance() {
        return instance;
    }

    public synchronized <T> void send(List<String> headerKeys, List<T> headerValues, Packet packet) {
        MessageProperties messageProperties = new MessageProperties();
        for (int i = 0; i < headerKeys.size(); i++) {
            messageProperties.setHeader(headerKeys.get(i), headerValues.get(i));
        }
        Message message = new Message(packet.getRawPacket(), messageProperties);

        try {
            rabbitTemplate.convertAndSend(rabbitMQConfig.getExchangeName(), "chat-to-game", message);
        } catch (AmqpException ae) {
            log.error("Error while sending message to RabbitMQ: {}", ae.getMessage());
        }
    }

    public synchronized <T> void send(String headerKey, T headerValue, Packet packet) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader(headerKey, headerValue);
        Message message = new Message(packet.getRawPacket(), messageProperties);

        try {
            rabbitTemplate.convertAndSend(rabbitMQConfig.getExchangeName(), "chat-to-game", message);
        } catch (AmqpException ae) {
            log.error("Error while sending message to RabbitMQ: {}", ae.getMessage());
        }
    }

    public synchronized void send(Packet packet) {
        Message message = new Message(packet.getRawPacket());

        try {
            rabbitTemplate.convertAndSend(rabbitMQConfig.getExchangeName(), "chat-to-game", message);
        } catch (AmqpException ae) {
            log.error("Error while sending message to RabbitMQ: {}", ae.getMessage());
        }
    }
}
