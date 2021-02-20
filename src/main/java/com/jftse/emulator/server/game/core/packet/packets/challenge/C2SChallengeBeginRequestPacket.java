package com.jftse.emulator.server.game.core.packet.packets.challenge;

import com.jftse.emulator.server.networking.packet.Packet;
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
