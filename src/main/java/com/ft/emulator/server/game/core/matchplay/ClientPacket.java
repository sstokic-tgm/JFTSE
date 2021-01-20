package com.ft.emulator.server.game.core.matchplay;

import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientPacket {
    private Client client;
    private Packet packet;
}