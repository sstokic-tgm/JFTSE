package com.jftse.server.core.rabbit;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnProperty(prefix = "jftse.rabbitmq", name = "enabled", havingValue = "true")
public abstract class AbstractRabbitMQConfiguration implements RabbitContract {

    @Value("${jftse.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${jftse.rabbitmq.queue}")
    private String defaultQueueName;

    @Value("#{'${jftse.rabbitmq.binding}'.split(' ')}")
    private List<String> defaultBindingKeys;

    @Override
    public String getExchangeName() {
        return exchangeName;
    }

    @Override
    public String getQueueName() {
        return defaultQueueName;
    }

    @Override
    public List<String> getBindingKeys() {
        return defaultBindingKeys;
    }

    protected List<RabbitQueueDefinition> getAdditionalQueues() {
        return List.of();
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(getExchangeName());
    }

    @Bean
    public Declarables rabbitDeclarables() {
        List<Declarable> declarableList = new ArrayList<>();

        Queue defaultQueue = new Queue(getQueueName(), false);
        declarableList.add(defaultQueue);
        for (String bindingKey : getBindingKeys()) {
            declarableList.add(BindingBuilder.bind(defaultQueue).to(exchange()).with(bindingKey));
        }

        for (RabbitQueueDefinition queueDefinition : getAdditionalQueues()) {
            Queue queue = new Queue(queueDefinition.getQueueName(), false);
            declarableList.add(queue);
            for (String bindingKey : queueDefinition.getBindingKeys()) {
                declarableList.add(BindingBuilder.bind(queue).to(exchange()).with(bindingKey));
            }
        }

        return new Declarables(declarableList);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);

        factory.setDefaultRequeueRejected(false);

        Advice retryAdvice = RetryInterceptorBuilder.stateless()
                .maxAttempts(5)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
        factory.setAdviceChain(retryAdvice);

        return factory;
    }
}
