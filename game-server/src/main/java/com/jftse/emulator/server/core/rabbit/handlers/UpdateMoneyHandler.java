package com.jftse.emulator.server.core.rabbit.handlers;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.shop.S2CShopMoneyAnswerPacket;
import com.jftse.emulator.server.core.rabbit.MessageTypes;
import com.jftse.emulator.server.core.rabbit.messages.UpdateMoneyMessage;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.rabbit.AbstractMessageHandler;
import com.jftse.server.core.rabbit.MessageHandlerRegistry;
import com.jftse.server.core.service.PlayerService;
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
        log.info("Updating money on client for player {}", message.getPlayerId());

        final FTConnection connection = gameManager.getConnectionByPlayerId(message.getPlayerId());
        final Player player = playerService.findById(message.getPlayerId());
        if (player == null) {
            log.error("Player {} not found", message.getPlayerId());
            return;
        }

        S2CShopMoneyAnswerPacket moneyAnswerPacket = new S2CShopMoneyAnswerPacket(player);
        if (connection != null) {
            connection.sendTCP(moneyAnswerPacket);

            log.info("Money updated on client for player {}", message.getPlayerId());
        }
    }
}
