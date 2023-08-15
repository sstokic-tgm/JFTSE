package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.packets.inventory.C2SInventoryWearSpecialRequestPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearSpecialAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.SpecialSlotEquipmentService;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
@PacketOperationIdentifier(PacketOperations.C2SInventoryWearSpecialRequest)
public class InventoryWearSpecialPacketHandler extends AbstractPacketHandler {
    private C2SInventoryWearSpecialRequestPacket inventoryWearSpecialRequestPacket;

    private final SpecialSlotEquipmentService specialSlotEquipmentService;

    public InventoryWearSpecialPacketHandler() {
        specialSlotEquipmentService = ServiceManager.getInstance().getSpecialSlotEquipmentService();
    }

    @Override
    public boolean process(Packet packet) {
        inventoryWearSpecialRequestPacket = new C2SInventoryWearSpecialRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player player = client.getPlayer();

        specialSlotEquipmentService.updateSpecialSlots(player, inventoryWearSpecialRequestPacket.getSpecialSlotList());

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
