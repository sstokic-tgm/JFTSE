package com.jftse.emulator.server.core.handler.player;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.player.C2SPlayerNameChangePacket;
import com.jftse.emulator.server.core.packets.player.S2CPlayerNameChangeMessagePacket;
import com.jftse.emulator.server.core.packets.player.S2CPlayerNameChangePacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;

import java.util.*;

@PacketOperationIdentifier(PacketOperations.C2SPlayerNameChange)
public class PlayerNameChangeHandler extends AbstractPacketHandler {
    private C2SPlayerNameChangePacket packet;

    private final PlayerService playerService;
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;

    public PlayerNameChangeHandler() {
        this.playerService = ServiceManager.getInstance().getPlayerService();
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.pocketService = ServiceManager.getInstance().getPocketService();
    }

    @Override
    public boolean process(Packet packet) {
        this.packet = new C2SPlayerNameChangePacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null) {
            return;
        }

        Player player = playerService.findById((long) packet.getPlayerId());
        if (player == null || !player.getAccount().getId().equals(client.getAccountId())) {
            S2CPlayerNameChangeMessagePacket response1 = new S2CPlayerNameChangeMessagePacket(S2CPlayerNameChangeMessagePacket.MSG_CHARACTER_NOT_FOUND, null);
            S2CPlayerNameChangePacket response2 = new S2CPlayerNameChangePacket(S2CPlayerNameChangeMessagePacket.MSG_CHARACTER_NOT_FOUND, null);
            connection.sendTCP(response1, response2);
            return;
        }

        Date lastChangeDate = player.getLastNameChangeDate();
        Calendar nextChangeDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar currentCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if (lastChangeDate != null) {
            nextChangeDate.setTime(lastChangeDate);
            nextChangeDate.add(Calendar.DATE, 30);

            if (currentCalendar.before(nextChangeDate)) {
                S2CPlayerNameChangeMessagePacket response1 = new S2CPlayerNameChangeMessagePacket(S2CPlayerNameChangeMessagePacket.MSG_NAME_CHANGE_NEXT, player);
                S2CPlayerNameChangePacket response2 = new S2CPlayerNameChangePacket(S2CPlayerNameChangeMessagePacket.MSG_NAME_CHANGE_NEXT, player);
                connection.sendTCP(response1, response2);
                return;
            }
        }

        if (playerService.findByName(packet.getNewPlayerName()) != null) {
            S2CPlayerNameChangeMessagePacket response1 = new S2CPlayerNameChangeMessagePacket(S2CPlayerNameChangeMessagePacket.MSG_ALREADY_USE_NICKNAME, null);
            S2CPlayerNameChangePacket response2 = new S2CPlayerNameChangePacket(S2CPlayerNameChangeMessagePacket.MSG_ALREADY_USE_NICKNAME, null);
            connection.sendTCP(response1, response2);
            return;
        }

        Pocket pocket = pocketService.findById(player.getPocket().getId());
        List<PlayerPocket> inventory = playerPocketService.getPlayerPocketItems(pocket);
        Optional<PlayerPocket> optNameChangeItem = inventory.stream()
                .filter(pp -> pp.getCategory().equals(EItemCategory.SPECIAL.getName()) && pp.getItemIndex() == 4)
                .findFirst();
        if (optNameChangeItem.isPresent()) {
            PlayerPocket nameChangeItem = optNameChangeItem.get();
            player.setName(packet.getNewPlayerName());
            player.setLastNameChangeDate(currentCalendar.getTime());
            player.setNameChangeAllowed(false);

            playerService.save(player);

            playerPocketService.remove(nameChangeItem.getId());
            pocketService.decrementPocketBelongings(pocket);

            S2CPlayerNameChangeMessagePacket response1 = new S2CPlayerNameChangeMessagePacket(S2CPlayerNameChangeMessagePacket.RESULT_SUCCESS, player);
            S2CPlayerNameChangePacket response2 = new S2CPlayerNameChangePacket(S2CPlayerNameChangeMessagePacket.RESULT_SUCCESS, player);
            S2CInventoryItemRemoveAnswerPacket inventoryItemRemovePacket = new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(nameChangeItem.getId()));
            connection.sendTCP(response1, response2, inventoryItemRemovePacket);
        } else {
            S2CPlayerNameChangePacket response = new S2CPlayerNameChangePacket(S2CPlayerNameChangeMessagePacket.MSG_NAME_CHANGE_UNABLE, null);
            connection.sendTCP(response);
        }
    }
}
