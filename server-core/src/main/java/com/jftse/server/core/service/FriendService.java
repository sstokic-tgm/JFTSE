package com.jftse.server.core.service;

import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;

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
}
