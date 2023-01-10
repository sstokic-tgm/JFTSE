package com.jftse.server.core.handler;

import com.jftse.server.core.protocol.PacketOperations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PacketOperationIdentifier {
    PacketOperations value();
}
