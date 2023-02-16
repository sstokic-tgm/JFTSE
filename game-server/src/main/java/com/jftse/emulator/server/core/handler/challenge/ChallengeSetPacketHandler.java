package com.jftse.emulator.server.core.handler.challenge;

import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SChallengeSet)
public class ChallengeSetPacketHandler extends AbstractPacketHandler {
    @Override
    public boolean process(Packet packet) {
        return false;
    }

    @Override
    public void handle() {
        // empty
    }
}
