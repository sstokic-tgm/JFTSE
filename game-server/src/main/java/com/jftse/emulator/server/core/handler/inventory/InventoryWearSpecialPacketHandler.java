package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.life.item.ItemFactory;
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

import java.util.ArrayList;
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

        List<Integer> specialSlotListEquippedServer = specialSlotEquipmentService.getEquippedSpecialSlots(player);
        List<Integer> specialSlotListEquippedClient = inventoryWearSpecialRequestPacket.getSpecialSlotList();

        LogEquippedSlots(specialSlotListEquippedClient, "C2S received");

        boolean flagBackFromMatchplay = ItemFactory.GetFlagBackFromMatchplay();
        log.info("Back from matchplay: " + flagBackFromMatchplay);
        ItemFactory.SetBackFromMatchplay(false);

        S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket;

        if (!flagBackFromMatchplay) {
            inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(specialSlotListEquippedClient);
            specialSlotEquipmentService.updateSpecialSlots(player, specialSlotListEquippedClient);
            LogEquippedSlots(specialSlotListEquippedClient, "S2C sending");
        } else {
            inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(specialSlotListEquippedServer);
            LogEquippedSlots(specialSlotListEquippedServer, "S2C sending");
        }

        connection.sendTCP(inventoryWearSpecialAnswerPacket);
    }

    public void LogEquippedSlots(List<Integer> equippedSlots, String textSendOrReceive){
        int j = 1;
        for (Integer slotValue : equippedSlots) {
            log.info(textSendOrReceive + " equipped slots WearSpecial slot" + j + " value: " + slotValue);
            j++;
        }
    }
}
