package com.jftse.emulator.server.rabbit;

import lombok.Getter;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class RabbitMQConfig {
    @Value("${jftse.rabbitmq.queue.game-to-chat}")
    private String toChatQueueName;

    @Value("${jftse.rabbitmq.queue.chat-to-game}")
    private String toGameQueueName;

    @Value("${jftse.rabbitmq.exchange}")
    private String exchangeName;

    @Bean
    public Queue toChatQueue() {
        return new Queue(toChatQueueName, false);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding toChatBinding(Queue toChatQueue, DirectExchange exchange) {
        return BindingBuilder.bind(toChatQueue).to(exchange).with("game-to-chat");
    }
}
