package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.service.FriendService;
import com.jftse.server.core.service.GuildService;
import com.jftse.server.core.service.SocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SocialServiceImpl implements SocialService {
    private final FriendService friendService;
    private final GuildService guildService;

    @Override
    @Transactional(readOnly = true)
    public List<Friend> getFriendList(Player player, EFriendshipState friendshipState) {
        return friendService.findWithFriendByPlayer(player).stream()
                .filter(x -> x.getEFriendshipState() == friendshipState)
                .sorted(Comparator.comparing(p -> (!p.getFriend().getOnline())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friend> getFriendListByFriend(Player player, EFriendshipState friendshipState) {
        return friendService.findWithFriendByFriend(player).stream()
                .filter(x -> x.getEFriendshipState() == friendshipState)
                .sorted(Comparator.comparing(p -> (!p.getFriend().getOnline())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Friend getRelationship(Player player) {
        return friendService.findByPlayer(player).stream()
                .filter(x -> x.getEFriendshipState() == EFriendshipState.Relationship)
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Friend getRelationshipWithFriend(Player player) {
        return friendService.findWithFriendByPlayer(player).stream()
                .filter(x -> x.getEFriendshipState() == EFriendshipState.Relationship)
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuildMember> getGuildMemberList(Player player) {
        Guild guild = guildService.findWithMembersByPlayerId(player.getId());
        if (guild != null) {
            return guild.getMemberList().stream()
                    .filter(gm -> !gm.getPlayer().getId().equals(player.getId()) && !gm.getWaitingForApproval())
                    .sorted(Comparator.comparing(p -> (!p.getPlayer().getOnline())))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
