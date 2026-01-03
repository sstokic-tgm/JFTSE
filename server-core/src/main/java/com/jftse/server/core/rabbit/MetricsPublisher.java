package com.jftse.server.core.rabbit;

import com.jftse.emulator.common.utilities.RandomUtils;
import com.jftse.server.core.thread.ThreadManager;
import com.jftse.server.core.util.Time;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@ConditionalOnProperty(prefix = "jftse.rabbitmq.metrics", name = "enabled", havingValue = "true")
public class MetricsPublisher {
    @Getter private static MetricsPublisher instance;

    private final RabbitContract rabbitContract;
    private final RabbitTemplate rabbitTemplate;
    private final ThreadManager threadManager;

    @Autowired
    public MetricsPublisher(RabbitContract rabbitContract, RabbitTemplate rabbitTemplate, ThreadManager threadManager) {
        instance = this;

        this.rabbitContract = rabbitContract;
        this.rabbitTemplate = rabbitTemplate;
        this.threadManager = threadManager;
    }

    public void publish(AbstractBaseMessage message, String routingKey, String sender) {
        threadManager.newTask(() -> publish0(message, routingKey, sender));
    }

    private void publish0(AbstractBaseMessage message, String routingKey, String sender) {
        String[] routingKeys = routingKey.split("\\s+");
        for (String key : routingKeys) {
            message.setCorrelationId(RandomUtils.getUUID());
            message.setSender(sender);
            try {
                final long start = Time.getNSTime();

                log.debug("[{}] Publishing message to {}: type={}, sender={}",
                        message.getCorrelationId(),
                        key,
                        message.getMessageType(),
                        message.getSender());

                rabbitTemplate.convertAndSend(rabbitContract.getExchangeName(), key, message);

                final long duration = Time.nanoToMillis(Time.getNSTimeDiff(start, Time.getNSTime()));
                log.debug("[{}] Message published in {} ms",
                        message.getCorrelationId(),
                        duration);

            } catch (Exception e) {
                log.error("[{}] Failed to publish message to {}: type={}, sender={}. Error: {}",
                        message.getCorrelationId(),
                        key,
                        message.getMessageType(),
                        message.getSender(),
                        e.getMessage(), e);
            }
        }
    }
}
