package com.jftse.emulator.server.core.rabbit.service;

import com.jftse.emulator.common.utilities.RandomUtils;
import com.jftse.emulator.server.rabbit.RabbitMQConfig;
import com.jftse.server.core.rabbit.AbstractBaseMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void send(AbstractBaseMessage message, String routingKey, String sender) {
        String[] routingKeys = routingKey.split("\\s+");
        for (String key : routingKeys) {
            message.setCorrelationId(RandomUtils.getUUID());
            message.setSender(sender);
            try {
                final long start = System.currentTimeMillis();

                log.debug("[{}] Sending message to {}: type={}, sender={}",
                        message.getCorrelationId(),
                        key,
                        message.getMessageType(),
                        message.getSender());

                rabbitTemplate.convertAndSend(rabbitMQConfig.getExchangeName(), key, message);

                final long duration = System.currentTimeMillis() - start;
                log.debug("[{}] Message sent in {} ms",
                        message.getCorrelationId(),
                        duration);

            } catch (AmqpException ae) {
                log.error("[{}] Failed to send message: {}",
                        message.getCorrelationId(),
                        ae.getMessage());
            }
        }
    }
}
