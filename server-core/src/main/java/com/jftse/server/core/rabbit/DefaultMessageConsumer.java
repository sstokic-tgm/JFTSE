package com.jftse.server.core.rabbit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(prefix = "jftse.rabbitmq", name = "enabled", havingValue = "true")
public class DefaultMessageConsumer {
    private static final Logger log = LogManager.getLogger(DefaultMessageConsumer.class);
    private final DefaultMessageHandlerRegistry registry;

    public DefaultMessageConsumer(DefaultMessageHandlerRegistry registry,
                                  List<AbstractMessageHandler<? extends AbstractBaseMessage>> handlers) {
        this.registry = registry;

        handlers.forEach(handler -> {
            handler.register(registry);
            log.debug("Registered handler: {}", handler.getClass().getSimpleName());
        });

        log.info("DefaultMessageConsumer initialized");
    }

    @RabbitListener(queues = "${jftse.rabbitmq.queue}")
    public void receiveMessage(AbstractBaseMessage message) {
        final long start = System.currentTimeMillis();

        log.debug("[{}] Message received from {}: type={}",
                message.getCorrelationId(),
                message.getSender(),
                message.getMessageType());

        AbstractMessageHandler<AbstractBaseMessage> handler = registry.getHandler(message.getMessageType());
        if (handler != null) {
            handler.handle(message);
        } else {
            log.warn("[{}] No handler found for message type: {}",
                    message.getCorrelationId(),
                    message.getMessageType());
        }

        final long duration = System.currentTimeMillis() - start;
        log.debug("[{}] Message processed in {} ms",
                message.getCorrelationId(),
                duration);
    }
}
