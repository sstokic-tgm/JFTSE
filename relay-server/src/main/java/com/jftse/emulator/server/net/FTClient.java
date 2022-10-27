package com.jftse.emulator.server.net;

import com.jftse.server.core.net.Client;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FTClient extends Client<FTConnection> {
    private Integer gameSessionId;
}
