package com.ft.emulator.server.game.server.packets.challenge;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SChallengeBeginRequestPacket extends Packet {

    private char challengeId;

    public C2SChallengeBeginRequestPacket(Packet packet) {

        super(packet);

        this.challengeId = packet.readChar();
    }
}