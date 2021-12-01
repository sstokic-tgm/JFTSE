package com.jftse.emulator.server.core.handler.game.chat;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packet.packets.chat.C2SWhisperReqPacket;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.chat.S2CWhisperAnswerPacket;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.Optional;

public class ChatMessageWhisperPacketHandler extends AbstractHandler {
    private C2SWhisperReqPacket whisperReqPacket;

    @Override
    public boolean process(Packet packet) {
        whisperReqPacket = new C2SWhisperReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        S2CWhisperAnswerPacket whisperAnswerPacket = new S2CWhisperAnswerPacket(connection.getClient().getActivePlayer().getName(), whisperReqPacket.getReceiverName(), whisperReqPacket.getMessage());

        Optional<Client> optClientToWhisper = GameManager.getInstance().getClients().stream()
                .filter(cl -> cl.getActivePlayer() != null && cl.getActivePlayer().getName().equalsIgnoreCase(whisperReqPacket.getReceiverName()))
                .findAny();
        if (optClientToWhisper.isPresent()) {
            connection.getServer().sendToTcp(optClientToWhisper.get().getConnection().getId(), whisperAnswerPacket);
            connection.sendTCP(whisperAnswerPacket);
        } else {
            S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "Whisper", "This user not connected.");
            connection.sendTCP(chatLobbyAnswerPacket);
        }
    }
}
