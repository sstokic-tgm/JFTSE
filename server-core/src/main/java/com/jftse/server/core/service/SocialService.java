package com.jftse.server.core.service;

import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;

import java.util.List;

public interface SocialService {
    List<Friend> getFriendList(Player player, EFriendshipState friendshipState);

    List<Friend> getFriendListByFriend(Player player, EFriendshipState friendshipState);

    Friend getRelationship(Player player);

    List<GuildMember> getGuildMemberList(Player player);
}
