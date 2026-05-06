package com.jftse.emulator.server.core.handler.challenge;

import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.challenge.CMSGChallengeSet;

@PacketId(CMSGChallengeSet.PACKET_ID)
public class ChallengeSetPacketHandler implements PacketHandler<FTConnection, CMSGChallengeSet> {
    @Override
    public void handle(FTConnection connection, CMSGChallengeSet packet) {
        // empty
    }
}
