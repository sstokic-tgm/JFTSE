package com.jftse.entities.database.repository.messenger;

import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    Optional<Friend> findById(Long id);
    List<Friend> findByPlayer(Player player);
    List<Friend> findByFriend(Player friend);
    Friend findByPlayerIdAndFriendId(Long playerId, Long friendId);
    long deleteAllByPlayer(Player player);
    long deleteAllByFriend(Player friend);
}