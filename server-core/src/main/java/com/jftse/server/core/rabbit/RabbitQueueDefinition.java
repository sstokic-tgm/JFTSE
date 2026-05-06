package com.jftse.server.core.rabbit;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class RabbitQueueDefinition {
    private final String queueName;
    private final List<String> bindingKeys;
}
