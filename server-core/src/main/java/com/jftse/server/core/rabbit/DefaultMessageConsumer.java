package com.jftse.server.core.rabbit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    private Gson gsonDefault;

    public DefaultMessageConsumer(DefaultMessageHandlerRegistry registry,
                                  List<AbstractMessageHandler<? extends AbstractBaseMessage>> handlers) {
        this.registry = registry;

        handlers.forEach(handler -> {
            handler.register(registry);
            log.debug("Registered handler: {}", handler.getClass().getSimpleName());
        });

        log.info("DefaultMessageConsumer initialized");
    }

    /**
     * Set a custom Gson instance for serialization/deserialization.
     * If not set, a default Gson instance will be created with pretty printing and null serialization.
     *
     * @param gson the custom Gson instance
     */
    public void setGson(Gson gson) {
        this.gsonDefault = gson;
    }

    /**
     * Get the Gson instance for serialization/deserialization.
     * If a custom Gson instance is not set, a default Gson instance will be created with pretty printing and null serialization.
     * This method can be overridden to provide a custom Gson instance if needed.
     *
     * @return the Gson instance
     */
    public Gson getGson() {
        if (gsonDefault == null) {
            gsonDefault = new GsonBuilder()
                    .setPrettyPrinting()
                    .serializeNulls()
                    .create();
        }
        return gsonDefault;
    }

    @RabbitListener(queues = "${jftse.rabbitmq.queue}")
    public void receiveMessage(AbstractBaseMessage message) {
        final long start = System.currentTimeMillis();

        log.debug("[{}] Message received from {}: type={}",
                message.getCorrelationId(),
                message.getSender(),
                message.getMessageType());

        Gson gson = getGson();
        log.debug("[{}]\n{} {}",
                message.getCorrelationId(),
                message.getClass().getSimpleName(),
                gson.toJson(message));

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
