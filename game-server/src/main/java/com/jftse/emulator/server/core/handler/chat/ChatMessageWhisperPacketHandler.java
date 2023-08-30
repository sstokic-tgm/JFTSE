package com.jftse.emulator.server.core.handler.chat;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.chat.C2SWhisperReqPacket;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packets.chat.S2CWhisperAnswerPacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.List;
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
        FTClient client = (FTClient) connection.getClient();

        Optional<FTClient> optClientToWhisper = GameManager.getInstance().getClients().stream()
                .filter(cl -> cl.getPlayer() != null && cl.getPlayer().getName().equalsIgnoreCase(whisperReqPacket.getReceiverName()))
                .findAny();
        if (optClientToWhisper.isPresent()) {
            S2CWhisperAnswerPacket whisperAnswerPacket = new S2CWhisperAnswerPacket(client.getPlayer().getName(), whisperReqPacket.getReceiverName(), whisperReqPacket.getMessage());

            optClientToWhisper.get().getConnection().sendTCP(whisperAnswerPacket);
            connection.sendTCP(whisperAnswerPacket);
        } else {
            Player player = ServiceManager.getInstance().getPlayerService().findByName(whisperReqPacket.getReceiverName());
            if (player != null) {
                S2CWhisperAnswerPacket whisperAnswerPacket = new S2CWhisperAnswerPacket(client.getPlayer().getName(), whisperReqPacket.getReceiverName(), whisperReqPacket.getMessage());
                RProducerService.getInstance().send(List.of("playerId", "senderPlayerId"), List.of(player.getId(), client.getActivePlayerId()), whisperAnswerPacket);
            } else {
                S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "Whisper", "This user does not exist.");
                connection.sendTCP(chatLobbyAnswerPacket);
            }
        }
    }
}
