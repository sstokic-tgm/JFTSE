package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.messenger.S2CParcelListPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CReceivedParcelNotificationPacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.model.log.GameLogType;
import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GameLogService;
import com.jftse.server.core.service.ParcelService;
import com.jftse.server.core.service.ParcelTransactionService;
import com.jftse.server.core.service.transaction.ParcelTransactionResult;
import com.jftse.server.core.service.transaction.TransactionException;
import com.jftse.server.core.service.transaction.TransactionNotifications;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.server.core.shared.packets.messenger.CMSGSendParcel;
import com.jftse.server.core.shared.packets.messenger.SMSGSendParcel;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;

import java.util.List;

@PacketId(CMSGSendParcel.PACKET_ID)
public class SendParcelRequestHandler implements PacketHandler<FTConnection, CMSGSendParcel> {
    private final ParcelTransactionService parcelTransactionService;
    private final ParcelService parcelService;
    private final GameLogService gameLogService;
    private final RProducerService rProducerService;

    public SendParcelRequestHandler() {
        parcelTransactionService = ServiceManager.getInstance().getParcelTransactionService();
        parcelService = ServiceManager.getInstance().getParcelService();
        gameLogService = ServiceManager.getInstance().getGameLogService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGSendParcel packet) {
        FTClient client = connection.getClient();
        if (client == null || !client.hasPlayer()) {
            connection.sendTCP(SMSGSendParcel.builder().status((short) -1).build());
            return;
        }

        ParcelTransactionResult result;
        try {
            result = parcelTransactionService.send(client.getAccountId(), client.getPlayer().getId(),
                    (long) packet.getPlayerPocketId(), packet.getReceiverName(), packet.getCashOnDelivery(), packet.getMessage());
        } catch (TransactionException exception) {
            if (exception.getReason() == TransactionException.Reason.NOT_FOUND) {
                return;
            }
            if (exception.getReason() == TransactionException.Reason.FORBIDDEN) {
                GameLog gameLog = new GameLog();
                gameLog.setGameLogType(GameLogType.BANABLE);
                gameLog.setContent("pockets are not equal! requested playerPocketId: " + packet.getPlayerPocketId()
                        + ", requesting player pocketId: " + client.getPlayer().getPocketId()
                        + ", requesting playerId: " + client.getPlayer().getId());
                gameLogService.save(gameLog);
            }
            short status = switch (exception.getReason()) {
                case INSUFFICIENT_GOLD -> -2;
                case LEVEL_TOO_LOW -> -4;
                case INVALID_AMOUNT -> -5;
                default -> -1;
            };
            connection.sendTCP(SMSGSendParcel.builder().status(status).build());
            return;
        } catch (ArithmeticException exception) {
            connection.sendTCP(SMSGSendParcel.builder().status((short) -1).build());
            return;
        }

        TransactionNotifications.afterCommit(() -> {
            client.getPlayer().syncGold(result.gold());
            Parcel parcel = result.parcel();
            S2CReceivedParcelNotificationPacket notification = new S2CReceivedParcelNotificationPacket(parcel);
            PacketMessage message = PacketMessage.builder()
                    .receivingPlayerId(parcel.getReceiver().getId())
                    .packet(notification)
                    .build();
            rProducerService.send(message, "game.messenger.parcel chat.messenger.parcel",
                    client.getPlayer().getName() + "(GameServer)");

            connection.sendTCP(SMSGSendParcel.builder().status((short) 0).build());
            connection.sendTCP(new S2CInventoryItemRemoveAnswerPacket(result.item().getId().intValue()));
            List<Parcel> sentParcels = parcelService.findWithPlayerBySender(client.getPlayer().getId());
            connection.sendTCP(new S2CParcelListPacket((byte) 1, sentParcels));
        });
    }
}
