package com.jftse.server.core.service;

import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FriendService {
    Friend save(Friend friend);

    void remove(Long friendMemberId);

    Friend findById(Long id);

    List<Friend> findByPlayer(Player player);

    List<Friend> findByFriend(Player friend);

    Friend findByPlayerIdAndFriendId(long playerId, long friendId);

    long deleteAllByPlayer(Player player);

    long deleteAllByFriend(Player friend);

    List<Friend> findWithFriendByPlayer(Player player);
    List<Friend> findWithPlayerByFriend(Player player);
}
