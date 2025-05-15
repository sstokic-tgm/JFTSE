package com.jftse.server.core.rabbit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractBaseMessage {
    private String correlationId;
    private String sender;

    public abstract String getMessageType();
}
