package com.jftse.server.core.rabbit;

import java.util.List;

public interface RabbitContract {
    String getExchangeName();
    String getQueueName();
    List<String> getBindingKeys();
}
