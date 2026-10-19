package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.life.event.GameEventBus;
import com.jftse.emulator.server.core.life.event.GameEventType;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventorySellItemAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.InventorySaleService;
import com.jftse.server.core.service.transaction.*;
import com.jftse.server.core.shared.packets.inventory.CMSGInventorySellItemCheck;
import com.jftse.server.core.shared.packets.inventory.SMSGInventorySellItemCheck;
import com.jftse.server.core.shared.packets.shop.SMSGSetMoney;

@PacketId(CMSGInventorySellItemCheck.PACKET_ID)
public class InventorySellItemCheckRequestHandler implements PacketHandler<FTConnection, CMSGInventorySellItemCheck> {
    private final InventorySaleService inventorySaleService;

    public InventorySellItemCheckRequestHandler() {
        inventorySaleService = ServiceManager.getInstance().getInventorySaleService();
    }

    @Override
    public void handle(FTConnection connection, CMSGInventorySellItemCheck packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer()) {
            return;
        }
        InventorySaleResult result;
        try {
            result = inventorySaleService.sell(client.getAccountId(), client.getPlayer().getId(), (long) packet.getItemPocketId());
        } catch (TransactionException exception) {
            byte status = (byte) (exception.getReason() == TransactionException.Reason.NO_ITEM ? -1 : -2);
            connection.sendTCP(SMSGInventorySellItemCheck.builder().status(status).build());
            return;
        } catch (ArithmeticException exception) {
            connection.sendTCP(SMSGInventorySellItemCheck.builder().status((byte) -2).build());
            return;
        }
        TransactionNotifications.afterCommit(() -> {
            client.getPlayer().syncGold(result.gold());
            client.getAp().set(result.ap());
            connection.sendTCP(SMSGInventorySellItemCheck.builder().status((byte) 0).build());
            for (int remaining = result.quantity(); remaining > 0; remaining -= Math.min(500, remaining)) {
                connection.sendTCP(new S2CInventorySellItemAnswerPacket((char) Math.min(500, remaining), result.itemPocketId()));
            }
            SMSGSetMoney moneyPacket = SMSGSetMoney.builder()
                    .gold(result.gold())
                    .ap(result.ap())
                    .build();
            connection.sendTCP(moneyPacket);
            GameEventBus.call(GameEventType.INVENTORY_ITEM_SOLD, client, result.transaction());
        });
    }
}
