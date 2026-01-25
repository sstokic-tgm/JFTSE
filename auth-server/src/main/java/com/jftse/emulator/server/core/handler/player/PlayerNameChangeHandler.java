package com.jftse.emulator.server.core.handler.player;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.auth.CMSGPlayerNameChange;
import com.jftse.server.core.shared.packets.auth.SMSGPlayerNameChange;
import com.jftse.server.core.shared.packets.auth.SMSGPlayerNameChangeMessage;
import com.jftse.server.core.shared.packets.inventory.SMSGInventoryRemoveItem;

import java.util.*;

@PacketId(CMSGPlayerNameChange.PACKET_ID)
public class PlayerNameChangeHandler implements PacketHandler<FTConnection, CMSGPlayerNameChange> {
    private final PlayerService playerService;
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;

    public static final byte RESULT_SUCCESS = 0;
    public static final byte MSG_NAME_CHANGE_NEXT = -4;
    public static final byte MSG_NAME_CHANGE_UNABLE = -3;
    public static final byte MSG_ALREADY_USE_NICKNAME = -2;
    public static final byte MSG_CHARACTER_NOT_FOUND = -1;

    public PlayerNameChangeHandler() {
        this.playerService = ServiceManager.getInstance().getPlayerService();
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.pocketService = ServiceManager.getInstance().getPocketService();
    }

    @Override
    public void handle(FTConnection connection, CMSGPlayerNameChange packet) {
        FTClient client = connection.getClient();
        if (client == null) {
            return;
        }

        Player player = playerService.findById((long) packet.getPlayerId());
        if (player == null || !player.getAccount().getId().equals(client.getAccountId())) {
            SMSGPlayerNameChangeMessage response1 = SMSGPlayerNameChangeMessage.builder().result(MSG_CHARACTER_NOT_FOUND).build();
            SMSGPlayerNameChange response2 = SMSGPlayerNameChange.builder().result(MSG_CHARACTER_NOT_FOUND).build();
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
                SMSGPlayerNameChangeMessage response1 = SMSGPlayerNameChangeMessage.builder().result(MSG_NAME_CHANGE_NEXT).nextChangeTime(nextChangeDate.getTime()).build();
                SMSGPlayerNameChange response2 = SMSGPlayerNameChange.builder().result(MSG_NAME_CHANGE_NEXT).build();
                connection.sendTCP(response1, response2);
                return;
            }
        }

        if (playerService.findByName(packet.getNewPlayerName()) != null) {
            SMSGPlayerNameChangeMessage response1 = SMSGPlayerNameChangeMessage.builder().result(MSG_ALREADY_USE_NICKNAME).build();
            SMSGPlayerNameChange response2 = SMSGPlayerNameChange.builder().result(MSG_ALREADY_USE_NICKNAME).build();
            connection.sendTCP(response1, response2);
            return;
        }

        Pocket pocket = pocketService.findById(player.getPocket().getId());
        PlayerPocket nameChangeItem = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(4, EItemCategory.SPECIAL.getName(), pocket);
        if (nameChangeItem != null) {
            player.setName(packet.getNewPlayerName());
            player.setLastNameChangeDate(currentCalendar.getTime());
            player.setNameChangeAllowed(false);

            playerService.save(player);

            playerPocketService.remove(nameChangeItem.getId());
            pocketService.decrementPocketBelongings(pocket);

            SMSGPlayerNameChangeMessage response1 = SMSGPlayerNameChangeMessage.builder()
                    .result(RESULT_SUCCESS)
                    .playerId(Math.toIntExact(player.getId()))
                    .name(player.getName())
                    .build();
            SMSGPlayerNameChange response2 = SMSGPlayerNameChange.builder()
                    .result(RESULT_SUCCESS)
                    .playerId(Math.toIntExact(player.getId()))
                    .name(player.getName())
                    .build();
            SMSGInventoryRemoveItem inventoryRemoveItem = SMSGInventoryRemoveItem.builder()
                    .itemPocketId(Math.toIntExact(nameChangeItem.getId()))
                    .build();
            connection.sendTCP(response1, response2, inventoryRemoveItem);
        } else {
            SMSGPlayerNameChange response = SMSGPlayerNameChange.builder().result(MSG_NAME_CHANGE_UNABLE).build();
            connection.sendTCP(response);
        }
    }
}
