package com.jftse.emulator.server.net;

import com.jftse.server.core.net.Client;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class FTClient extends Client<FTConnection> {
    private Optional<Integer> gameSessionId;

    public void setGameSessionId(Integer gameSessionId) {
        this.gameSessionId = Optional.of(gameSessionId);
    }
}
