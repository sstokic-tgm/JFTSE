package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearSpecialAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.SpecialSlotEquipmentService;
import com.jftse.server.core.shared.packets.inventory.CMSGInventoryWearSpecial;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
@PacketId(CMSGInventoryWearSpecial.PACKET_ID)
public class InventoryWearSpecialPacketHandler implements PacketHandler<FTConnection, CMSGInventoryWearSpecial> {
    private final SpecialSlotEquipmentService specialSlotEquipmentService;

    public InventoryWearSpecialPacketHandler() {
        specialSlotEquipmentService = ServiceManager.getInstance().getSpecialSlotEquipmentService();
    }

    @Override
    public void handle(FTConnection connection, CMSGInventoryWearSpecial packet) {
        FTClient client = connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player player = client.getPlayer();

        specialSlotEquipmentService.updateSpecialSlots(player, packet.getSpecialSlotList());

        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer != null) {
            if (roomPlayer.isFitting()) {
                player = client.getPlayer();
                roomPlayer.setSpecialSlotEquipmentId(player.getSpecialSlotEquipment().getId());
            }
        }

        List<Integer> specialSlotList = specialSlotEquipmentService.getEquippedSpecialSlots(player);
        S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(specialSlotList);
        connection.sendTCP(inventoryWearSpecialAnswerPacket);
    }
}
