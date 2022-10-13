package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.service.FriendService;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.SocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class SocialServiceImpl implements SocialService {
    private final FriendService friendService;
    private final GuildMemberService guildMemberService;

    @Override
    public List<Friend> getFriendList(Player player, EFriendshipState friendshipState) {
        return friendService.findByPlayer(player).stream()
                .filter(x -> x.getEFriendshipState() == friendshipState)
                .sorted(Comparator.comparing(p -> (!p.getFriend().getOnline())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Friend> getFriendListByFriend(Player player, EFriendshipState friendshipState) {
        return friendService.findByFriend(player).stream()
                .filter(x -> x.getEFriendshipState() == friendshipState)
                .sorted(Comparator.comparing(p -> (!p.getFriend().getOnline())))
                .collect(Collectors.toList());
    }

    @Override
    public Friend getRelationship(Player player) {
        return friendService.findByPlayer(player).stream()
                .filter(x -> x.getEFriendshipState() == EFriendshipState.Relationship)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<GuildMember> getGuildMemberList(Player player) {
        GuildMember guildMember = guildMemberService.getByPlayer(player);
        if (guildMember != null) {
            Guild guild = guildMember.getGuild();
            if (guild != null) {
                return guild.getMemberList().stream()
                        .filter(x -> x != guildMember && !x.getWaitingForApproval())
                        .sorted(Comparator.comparing(p -> (!p.getPlayer().getOnline())))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}
