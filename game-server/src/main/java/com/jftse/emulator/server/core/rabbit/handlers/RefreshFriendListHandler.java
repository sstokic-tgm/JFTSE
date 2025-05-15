package com.jftse.emulator.server.core.rabbit.handlers;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.messenger.S2CFriendsListAnswerPacket;
import com.jftse.emulator.server.core.rabbit.MessageTypes;
import com.jftse.emulator.server.core.rabbit.messages.RefreshFriendListMessage;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.rabbit.AbstractMessageHandler;
import com.jftse.server.core.rabbit.MessageHandlerRegistry;
import com.jftse.server.core.service.FriendService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.SocialService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class RefreshFriendListHandler extends AbstractMessageHandler<RefreshFriendListMessage> {
    @Autowired
    private GameManager gameManager;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private SocialService socialService;
    @Autowired
    private FriendService friendService;

    @Override
    public void register(MessageHandlerRegistry registry) {
        registry.register(MessageTypes.REFRESH_FRIEND_LIST.getValue(), this);
    }

    @Override
    public void handle(RefreshFriendListMessage message) {
        log.info("Player {} requested friend list refresh, notifying friends", message.getPlayerId());

        Player player = playerService.findById(message.getPlayerId());
        if (player == null) {
            log.error("Player {} not found", message.getPlayerId());
            return;
        }

        AtomicInteger notifyCount = new AtomicInteger();
        List<Friend> friends = friendService.findByPlayer(player);
        friends.forEach(x -> {
            List<Friend> friendList = socialService.getFriendList(x.getFriend(), EFriendshipState.Friends);
            S2CFriendsListAnswerPacket friendListAnswerPacket = new S2CFriendsListAnswerPacket(friendList);
            FTConnection friendConnection = gameManager.getConnectionByPlayerId(x.getFriend().getId());
            if (friendConnection != null) {
                friendConnection.sendTCP(friendListAnswerPacket);
                notifyCount.getAndIncrement();
            }
        });

        log.info("Notified {} friends", notifyCount.get());
    }
}
