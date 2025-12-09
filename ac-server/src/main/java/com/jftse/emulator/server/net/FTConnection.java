package com.jftse.emulator.server.net;

import com.jftse.entities.database.model.ServerType;
import com.jftse.server.core.net.Connection;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@Log4j2
public class FTConnection extends Connection<FTClient> {
    private String hwid;

    private long lastReadTime = 0L;

    public FTConnection(final int decryptionKey, final int encryptionKey, final ServerType serverType) {
        super(decryptionKey, encryptionKey, serverType);
    }
}
