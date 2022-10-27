package com.jftse.emulator.server.core.handler.chat;

import com.jftse.emulator.server.core.packets.chat.C2SWhisperReqPacket;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packets.chat.S2CWhisperAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.Optional;

@PacketOperationIdentifier(PacketOperations.C2SWhisperReq)
public class ChatMessageWhisperPacketHandler extends AbstractPacketHandler {
    private C2SWhisperReqPacket whisperReqPacket;

    @Override
    public boolean process(Packet packet) {
        whisperReqPacket = new C2SWhisperReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = connection.getClient();
        S2CWhisperAnswerPacket whisperAnswerPacket = new S2CWhisperAnswerPacket(client.getPlayer().getName(), whisperReqPacket.getReceiverName(), whisperReqPacket.getMessage());

        Optional<FTClient> optClientToWhisper = GameManager.getInstance().getClients().stream()
                .filter(cl -> cl.getPlayer() != null && cl.getPlayer().getName().equalsIgnoreCase(whisperReqPacket.getReceiverName()))
                .findAny();
        if (optClientToWhisper.isPresent()) {
            optClientToWhisper.get().getConnection().sendTCP(whisperAnswerPacket);
            connection.sendTCP(whisperAnswerPacket);
        } else {
            S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "Whisper", "This user not connected.");
            connection.sendTCP(chatLobbyAnswerPacket);
        }
    }
}
