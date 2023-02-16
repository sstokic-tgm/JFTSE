package com.jftse.server.core.service;

import com.jftse.entities.database.model.guild.Guild;

import java.util.List;

public interface GuildService {
    Guild save(Guild guild);

    void remove(Long guildId);

    Guild findByName(String name);

    List<Guild> findAllByNameContaining(String name);

    Guild findById(Long id);

    List<Guild> findAll();
}
