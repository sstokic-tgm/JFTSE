package com.jftse.emulator.server.rabbit;

import com.jftse.server.core.rabbit.AbstractRabbitMQConfiguration;
import com.jftse.server.core.rabbit.RabbitQueueDefinition;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
@Getter
@Log4j2
public class RabbitMQConfig extends AbstractRabbitMQConfiguration {
    @PostConstruct
    public void init() {
        log.info("RabbitMQ configuration initialized");
    }

    /**
     * Add additional queues with their binding keys
     * ex:
     * return List.of(
     *    new RabbitQueueDefinition("queueName", List.of("bindingKey1", "bindingKey2")),
     *    new RabbitQueueDefinition("queueName2", List.of("bindingKey3"))
     *    );
     */
    @Override
    protected List<RabbitQueueDefinition> getAdditionalQueues() {
        return List.of();
    }
}
