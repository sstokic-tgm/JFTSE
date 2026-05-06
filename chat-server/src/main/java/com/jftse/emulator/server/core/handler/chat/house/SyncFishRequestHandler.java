package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.housing.Fish;
import com.jftse.emulator.server.core.life.housing.FishManager;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.chat.house.CMSGSyncFish;
import com.jftse.server.core.shared.packets.chat.house.SMSGInitFishWithDetails;

import java.util.List;
import java.util.Optional;

@PacketId(CMSGSyncFish.PACKET_ID)
public class SyncFishRequestHandler implements PacketHandler<FTConnection, CMSGSyncFish> {
    @Override
    public void handle(FTConnection connection, CMSGSyncFish syncFishPacket) {
        FTClient client = connection.getClient();
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
            SMSGInitFishWithDetails initFishDetails = SMSGInitFishWithDetails.builder()
                    .fishId(fish.getId())
                    .fishModel(fish.getModel())
                    .fishState((byte) fish.getState().getValue())
                    .unk0(0.0f)
                    .z(fish.getZ())
                    .unk1(0.0f)
                    .x(fish.getX())
                    .y(fish.getY())
                    .dirX(fish.getDirX())
                    .dirY(fish.getDirY())
                    .destX(fish.getDestX())
                    .destY(fish.getDestY())
                    .speed(fish.getSpeed())
                    .unk5(0.0f)
                    .unk6(0.0f)
                    .unk7((short) 0)
                    .build();
            connection.sendTCP(initFishDetails);
        }
    }
}
