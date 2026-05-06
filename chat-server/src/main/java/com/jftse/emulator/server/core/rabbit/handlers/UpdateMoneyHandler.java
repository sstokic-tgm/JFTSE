package com.jftse.emulator.server.core.rabbit.handlers;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.rabbit.MessageTypes;
import com.jftse.emulator.server.core.rabbit.messages.UpdateMoneyMessage;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.rabbit.AbstractMessageHandler;
import com.jftse.server.core.rabbit.MessageHandlerRegistry;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.shared.packets.shop.SMSGSetMoney;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class UpdateMoneyHandler extends AbstractMessageHandler<UpdateMoneyMessage> {
    @Autowired
    private GameManager gameManager;
    @Autowired
    private PlayerService playerService;

    @Override
    public void register(MessageHandlerRegistry registry) {
        registry.register(MessageTypes.UPDATE_PLAYER_MONEY.getValue(), this);
    }

    @Override
    public void handle(UpdateMoneyMessage message) {
        final FTConnection connection = gameManager.getConnectionByPlayerId(message.getPlayerId());
        if (connection == null || connection.getClient() == null) {
            return;
        }

        FTClient client = connection.getClient();
        if (!client.hasPlayer()) {
            log.error("Player {} not found", message.getPlayerId());
            return;
        }

        Player dbPlayer = playerService.findWithAccountById(client.getPlayer().getId());
        SMSGSetMoney moneyPacket = SMSGSetMoney.builder()
                .ap(dbPlayer.getAccount().getAp())
                .gold(dbPlayer.getGold())
                .build();
        connection.sendTCP(moneyPacket);

        client.getAp().compareAndSet(client.getAp().get(), dbPlayer.getAccount().getAp());
        client.getPlayer().syncGold(dbPlayer.getGold());

        log.info("Money updated on client for player {}", message.getPlayerId());
    }
}
