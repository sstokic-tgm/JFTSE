package com.jftse.server.core.service;

import com.jftse.entities.database.model.guild.Guild;

import java.util.List;

public interface GuildService {
    Guild save(Guild guild);

    void remove(Long guildId);

    Guild findByName(String name);

    List<Guild> findAllByNameContaining(String name);

    Guild findById(Long id);

    Guild findWithMembersById(Long id);

    Guild findWithMembersByPlayerId(Long playerId);

    List<Guild> findAll();

    List<Guild> findAll(int offset);

    Guild changeInformation(Long guildId, String introduction, byte minLevel, boolean isPublic, Byte[] allowedCharacterTypes);

    void changeReverseMemberStatus(Long guildId, int playerId, boolean isApproved);
}
