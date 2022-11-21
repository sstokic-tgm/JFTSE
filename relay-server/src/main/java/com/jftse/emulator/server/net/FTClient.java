package com.jftse.emulator.server.net;

import com.jftse.server.core.net.Client;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
public class FTClient extends Client<FTConnection> {
    private Optional<Integer> gameSessionId = Optional.empty();

    private final AtomicBoolean isClosingConnection;

    public FTClient() {
        isClosingConnection = new AtomicBoolean(false);
    }

    public void setGameSessionId(Integer gameSessionId) {
        this.gameSessionId = Optional.of(gameSessionId);
    }
}
