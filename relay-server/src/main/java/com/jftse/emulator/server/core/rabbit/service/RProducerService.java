package com.jftse.emulator.server.core.rabbit.service;

import com.jftse.emulator.common.utilities.RandomUtils;
import com.jftse.emulator.server.rabbit.RabbitMQConfig;
import com.jftse.server.core.rabbit.AbstractBaseMessage;
import com.jftse.server.core.thread.ThreadManager;
import com.jftse.server.core.util.Time;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RProducerService {
    @Getter
    private static RProducerService instance;

    private final RabbitMQConfig rabbitMQConfig;
    private final RabbitTemplate rabbitTemplate;
    private final ThreadManager threadManager;

    @Autowired
    public RProducerService(RabbitMQConfig rabbitMQConfig, RabbitTemplate rabbitTemplate, ThreadManager threadManager) {
        instance = this;

        this.rabbitMQConfig = rabbitMQConfig;
        this.rabbitTemplate = rabbitTemplate;
        this.threadManager = threadManager;
    }

    public void send(AbstractBaseMessage message, String routingKey, String sender) {
        threadManager.newTask(() -> send0(message, routingKey, sender));
    }

    private void send0(AbstractBaseMessage message, String routingKey, String sender) {
        String[] routingKeys = routingKey.split("\\s+");
        for (String key : routingKeys) {
            message.setCorrelationId(RandomUtils.getUUID());
            message.setSender(sender);
            try {
                final long start = Time.getNSTime();

                log.debug("[{}] Sending message to {}: type={}, sender={}",
                        message.getCorrelationId(),
                        key,
                        message.getMessageType(),
                        message.getSender());

                rabbitTemplate.convertAndSend(rabbitMQConfig.getExchangeName(), key, message);

                final long duration = Time.getNSTimeDiffToNow(start);
                log.debug("[{}] Message sent in {} ms",
                        message.getCorrelationId(),
                        Time.nanoToMillis(duration));

            } catch (AmqpException ae) {
                log.error("[{}] Failed to send message: {}",
                        message.getCorrelationId(),
                        ae.getMessage());
            }
        }
    }
}
