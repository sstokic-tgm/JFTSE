package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.messenger.FriendRepository;
import com.jftse.server.core.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class FriendServiceImpl implements FriendService {
    private final FriendRepository friendRepository;

    @Override
    public Friend save(Friend friend) {
        return friendRepository.save(friend);
    }

    @Override
    public void remove(Long friendMemberId) {
        friendRepository.deleteById(friendMemberId);
    }

    @Override
    public Friend findById(Long id) { return friendRepository.findById(id).orElse(null); }

    @Override
    public List<Friend> findByPlayer(Player player) { return friendRepository.findByPlayer(player); }

    @Override
    public List<Friend> findByFriend(Player friend) { return friendRepository.findByFriend(friend); }

    @Override
    public Friend findByPlayerIdAndFriendId(long playerId, long friendId) { return friendRepository.findByPlayerIdAndFriendId(playerId, friendId); }

    @Override
    public long deleteAllByPlayer(Player player) {
        return friendRepository.deleteAllByPlayer(player);
    }

    @Override
    public long deleteAllByFriend(Player friend) {
        return friendRepository.deleteAllByFriend(friend);
    }
}
