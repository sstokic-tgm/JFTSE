package com.jftse.emulator.server.core.handler.inventory;

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

        boolean doNotUpdateSlotsWhenUnreasonableValues = false;
        int i = 1;
        for (Integer slot : inventoryWearSpecialRequestPacket.getSpecialSlotList()) {
            log.info("C2S WearSpecial slot" + i + " value: " + slot);
            if (slot < 0)
                doNotUpdateSlotsWhenUnreasonableValues = true;
            i++;
        }

        if(!doNotUpdateSlotsWhenUnreasonableValues)
            specialSlotEquipmentService.updateSpecialSlots(player, inventoryWearSpecialRequestPacket.getSpecialSlotList());

        List<Integer> specialSlotList = specialSlotEquipmentService.getEquippedSpecialSlots(player);
        int j = 1;
        for (Integer slot : specialSlotList) {
            log.info("S2C sending equipped slots WearSpecial slot" + j + " value: " + slot);
            j++;
        }
        S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(specialSlotList);
        connection.sendTCP(inventoryWearSpecialAnswerPacket);
    }
}
