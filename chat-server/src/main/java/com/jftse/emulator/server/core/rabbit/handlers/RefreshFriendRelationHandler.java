package com.jftse.emulator.server.core.rabbit.handlers;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.messenger.S2CRelationshipAnswerPacket;
import com.jftse.emulator.server.core.rabbit.MessageTypes;
import com.jftse.emulator.server.core.rabbit.messages.RefreshFriendRelationMessage;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.rabbit.AbstractMessageHandler;
import com.jftse.server.core.rabbit.MessageHandlerRegistry;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.SocialService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RefreshFriendRelationHandler extends AbstractMessageHandler<RefreshFriendRelationMessage> {
    @Autowired
    private GameManager gameManager;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private SocialService socialService;

    @Override
    public void register(MessageHandlerRegistry registry) {
        registry.register(MessageTypes.REFRESH_FRIEND_RELATION.getValue(), this);
    }

    @Override
    public void handle(RefreshFriendRelationMessage message) {
        log.info("Player {} requested relationship refresh, notifying relationship", message.getPlayerId());

        Player player = playerService.findById(message.getPlayerId());
        if (player == null) {
            log.error("Player {} not found", message.getPlayerId());
            return;
        }

        Friend myRelation = socialService.getRelationship(player);
        if (myRelation == null) {
            return;
        }

        Friend friendRelation = socialService.getRelationship(myRelation.getFriend());
        FTConnection relationshipConnection = gameManager.getConnectionByPlayerId(myRelation.getFriend().getId());
        if (relationshipConnection != null && friendRelation != null) {
            S2CRelationshipAnswerPacket s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(friendRelation);
            relationshipConnection.sendTCP(s2CRelationshipAnswerPacket);

            log.info("Notified relationship");
        }
    }
}
