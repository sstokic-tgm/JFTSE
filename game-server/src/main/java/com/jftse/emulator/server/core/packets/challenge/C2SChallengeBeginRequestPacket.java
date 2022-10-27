package com.jftse.emulator.server.core.packets.challenge;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SChallengeBeginRequestPacket extends Packet {
    private char challengeId;

    public C2SChallengeBeginRequestPacket(Packet packet) {
        super(packet);
        this.challengeId = this.readChar();
    }
}
