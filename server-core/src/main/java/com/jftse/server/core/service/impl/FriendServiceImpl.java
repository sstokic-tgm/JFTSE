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
public class FriendServiceImpl implements FriendService {
    private final FriendRepository friendRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Friend save(Friend friend) {
        return friendRepository.save(friend);
    }

    @Override
    @Transactional
    public void remove(Long friendMemberId) {
        friendRepository.deleteById(friendMemberId);
    }

    @Override
    @Transactional(readOnly = true)
    public Friend findById(Long id) {
        return friendRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friend> findByPlayer(Player player) {
        return friendRepository.findByPlayer(player);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friend> findByFriend(Player friend) {
        return friendRepository.findByFriend(friend);
    }

    @Override
    @Transactional(readOnly = true)
    public Friend findByPlayerIdAndFriendId(long playerId, long friendId) {
        return friendRepository.findByPlayerIdAndFriendId(playerId, friendId);
    }

    @Override
    @Transactional
    public long deleteAllByPlayer(Player player) {
        return friendRepository.deleteAllByPlayer(player);
    }

    @Override
    @Transactional
    public long deleteAllByFriend(Player friend) {
        return friendRepository.deleteAllByFriend(friend);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friend> findWithFriendByPlayer(Player player) {
        return friendRepository.findWithFriendByPlayer(player);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friend> findWithFriendByFriend(Player player) {
        return friendRepository.findWithFriendByFriend(player);
    }
}
