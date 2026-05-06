package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.core.rabbit.messages.UpdateMoneyMessage;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.model.log.GameLogType;
import com.jftse.entities.database.model.messenger.EParcelType;
import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GameLogService;
import com.jftse.server.core.service.ParcelService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.shared.packets.messenger.CMSGAcceptParcel;
import com.jftse.server.core.shared.packets.messenger.SMSGAcceptParcel;
import com.jftse.server.core.shared.packets.messenger.SMSGDeleteParcel;
import com.jftse.server.core.shared.packets.shop.SMSGSetMoney;

import java.util.List;

@PacketId(CMSGAcceptParcel.PACKET_ID)
public class AcceptParcelRequestHandler implements PacketHandler<FTConnection, CMSGAcceptParcel> {
    private final ParcelService parcelService;
    private final PlayerService playerService;
    private final PlayerPocketService playerPocketService;

    private final GameLogService gameLogService;

    private final RProducerService rProducerService;

    public AcceptParcelRequestHandler() {
        parcelService = ServiceManager.getInstance().getParcelService();
        playerService = ServiceManager.getInstance().getPlayerService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        gameLogService = ServiceManager.getInstance().getGameLogService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGAcceptParcel packet) {
        Parcel parcel = parcelService.findById((long) packet.getParcelId());
        if (parcel == null) return;

        FTClient ftClient = connection.getClient();
        if (!ftClient.hasPlayer())
            return;

        FTPlayer player = ftClient.getPlayer();

        Player receiver = playerService.findById(parcel.getReceiver().getId());
        Player sender = playerService.findById(parcel.getSender().getId());

        if (!receiver.getId().equals(player.getId())) {
            SMSGAcceptParcel answer = SMSGAcceptParcel.builder().status((short) -1).build();
            connection.sendTCP(answer);

            GameLog gameLog = new GameLog();
            gameLog.setGameLogType(GameLogType.BANABLE);
            gameLog.setContent("receiver not same like accepting player! parcelId: " + parcel.getId() + ", receiverId: " + receiver.getId() + ", accepting playerId: " + player.getId());
            gameLogService.save(gameLog);

            return;
        }

        if (receiver.getLevel() < 20) {
            SMSGAcceptParcel answer = SMSGAcceptParcel.builder().status((short) -1).build();
            connection.sendTCP(answer);
            return;
        }

        if (parcel.getEParcelType().equals(EParcelType.CashOnDelivery)) {
            final int newGoldReceiver = receiver.getGold() - parcel.getGold();
            if (newGoldReceiver < 0) {
                SMSGAcceptParcel answer = SMSGAcceptParcel.builder().status((short) -2).build();
                connection.sendTCP(answer);
                return;
            }
            receiver.setGold(newGoldReceiver);
            player.syncGold(newGoldReceiver);
            playerService.setMoney(receiver, newGoldReceiver);

            final int newGoldSender = sender.getGold() + parcel.getGold();
            sender.setGold(newGoldSender);
            playerService.setMoney(sender, newGoldSender);
        }

        if (parcel.getEParcelType().equals(EParcelType.Gold)) {
            final int newGoldSender = sender.getGold() - parcel.getGold();
            playerService.setMoney(sender, newGoldSender);

            final int newGoldReceiver = receiver.getGold() + parcel.getGold();
            player.syncGold(Math.max(newGoldReceiver, 0));
            playerService.setMoney(receiver, newGoldReceiver);
        }

        Pocket receiverPocket = receiver.getPocket();
        PlayerPocket item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(parcel.getItemIndex(), parcel.getCategory(), receiverPocket);
        if (item == null) {
            item = new PlayerPocket();
            item.setCategory(parcel.getCategory());
            item.setItemCount(parcel.getItemCount());
            item.setItemIndex(parcel.getItemIndex());
            item.setUseType(parcel.getUseType());
            item.setPocket(receiverPocket);
            item.setEnchantStr(parcel.getEnchantStr());
            item.setEnchantSta(parcel.getEnchantSta());
            item.setEnchantDex(parcel.getEnchantDex());
            item.setEnchantWil(parcel.getEnchantWil());
            item.setEnchantElement(parcel.getEnchantElement());
            item.setEnchantLevel(parcel.getEnchantLevel());
        } else {
            item.setItemCount(item.getItemCount() + parcel.getItemCount());
        }

        item = playerPocketService.save(item);
        parcelService.remove(parcel.getId());

        UpdateMoneyMessage updateMoneyMessage = UpdateMoneyMessage.builder()
                .playerId(sender.getId())
                .build();
        rProducerService.send(updateMoneyMessage, "game.messenger.parcel chat.messenger.parcel", player.getName() + "(GameServer)");

        SMSGDeleteParcel answer = SMSGDeleteParcel.builder().parcelId(parcel.getId().intValue()).build();
        connection.sendTCP(answer);

        SMSGSetMoney moneyPacket = SMSGSetMoney.builder()
                .ap(ftClient.getAp().get())
                .gold(player.getGold())
                .build();
        connection.sendTCP(moneyPacket);

        S2CInventoryItemsPlacePacket inventoryItemsPlacePacket = new S2CInventoryItemsPlacePacket(List.of(item));
        connection.sendTCP(inventoryItemsPlacePacket);
    }
}
