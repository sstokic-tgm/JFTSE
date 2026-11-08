package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.core.rabbit.messages.UpdateMoneyMessage;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.model.log.GameLogType;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GameLogService;
import com.jftse.server.core.service.ParcelTransactionService;
import com.jftse.server.core.service.transaction.ParcelTransactionResult;
import com.jftse.server.core.service.transaction.TransactionException;
import com.jftse.server.core.service.transaction.TransactionNotifications;
import com.jftse.server.core.shared.packets.messenger.CMSGAcceptParcel;
import com.jftse.server.core.shared.packets.messenger.SMSGAcceptParcel;
import com.jftse.server.core.shared.packets.messenger.SMSGDeleteParcel;
import com.jftse.server.core.shared.packets.shop.SMSGSetMoney;

import java.util.List;

@PacketId(CMSGAcceptParcel.PACKET_ID)
public class AcceptParcelRequestHandler implements PacketHandler<FTConnection, CMSGAcceptParcel> {
    private final ParcelTransactionService parcelTransactionService;
    private final GameLogService gameLogService;
    private final RProducerService rProducerService;

    public AcceptParcelRequestHandler() {
        parcelTransactionService = ServiceManager.getInstance().getParcelTransactionService();
        gameLogService = ServiceManager.getInstance().getGameLogService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGAcceptParcel packet) {
        FTClient client = connection.getClient();
        if (client == null || !client.hasPlayer()) {
            return;
        }

        ParcelTransactionResult result;
        try {
            result = parcelTransactionService.accept(client.getAccountId(), client.getPlayer().getId(), (long) packet.getParcelId());
        } catch (TransactionException exception) {
            if (exception.getReason() == TransactionException.Reason.NOT_FOUND) {
                return;
            }
            if (exception.getReason() == TransactionException.Reason.FORBIDDEN) {
                GameLog gameLog = new GameLog();
                gameLog.setGameLogType(GameLogType.BANABLE);
                gameLog.setContent("receiver not same like accepting player! parcelId: " + packet.getParcelId()
                        + ", accepting playerId: " + client.getPlayer().getId());
                gameLogService.save(gameLog);
            }
            short status = (short) (exception.getReason() == TransactionException.Reason.INSUFFICIENT_GOLD ? -2 : -1);
            connection.sendTCP(SMSGAcceptParcel.builder().status(status).build());
            return;
        } catch (ArithmeticException exception) {
            connection.sendTCP(SMSGAcceptParcel.builder().status((short) -1).build());
            return;
        }

        TransactionNotifications.afterCommit(() -> {
            client.getPlayer().syncGold(result.gold());
            client.getAp().set(result.ap());
            UpdateMoneyMessage updateMoneyMessage = UpdateMoneyMessage.builder()
                    .playerId(result.parcel().getSender().getId())
                    .build();
            rProducerService.send(updateMoneyMessage, "game.messenger.parcel chat.messenger.parcel",
                    client.getPlayer().getName() + "(ChatServer)");

            SMSGDeleteParcel deletePacket = SMSGDeleteParcel.builder()
                    .parcelId(result.parcel().getId().intValue())
                    .build();
            SMSGSetMoney moneyPacket = SMSGSetMoney.builder()
                    .ap(result.ap())
                    .gold(result.gold())
                    .build();
            connection.sendTCP(deletePacket);
            connection.sendTCP(moneyPacket);
            connection.sendTCP(new S2CInventoryItemsPlacePacket(List.of(result.item())));
        });
    }
}
