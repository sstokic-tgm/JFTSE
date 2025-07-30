package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.housing.Fish;
import com.jftse.emulator.server.core.life.housing.FishManager;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.packets.chat.house.C2SSyncFishPacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CInitFishWithDetailsPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.List;
import java.util.Optional;

@PacketOperationIdentifier(PacketOperations.C2SSyncFish)
public class SyncFishRequestHandler extends AbstractPacketHandler {
    private C2SSyncFishPacket syncFishPacket;

    @Override
    public boolean process(Packet packet) {
        syncFishPacket = new C2SSyncFishPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null)
            return;

        Room room = client.getActiveRoom();
        if (room == null)
            return;

        final List<Fish> fishes = FishManager.getInstance().getFishes(room.getRoomId());
        if (fishes == null || fishes.isEmpty()) {
            return;
        }

        Optional<Fish> optFish = fishes.stream()
                .filter(fish -> fish.getId() == syncFishPacket.getFishId())
                .findFirst();
        if (optFish.isPresent()) {
            Fish fish = optFish.get();
            S2CInitFishWithDetailsPacket initFishDetails = new S2CInitFishWithDetailsPacket(
                    fish.getId(), fish.getModel(), (byte) fish.getState().getValue(),
                    0.0f, fish.getZ(), 0.0f, fish.getX(), fish.getY(),
                    fish.getDirX(), fish.getDirY(), fish.getDestX(), fish.getDestY(),
                    fish.getSpeed(), 0.0f, 0.0f, (short) 0
            );
            connection.sendTCP(initFishDetails);
        }
    }
}
