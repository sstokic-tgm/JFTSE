package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.repository.guild.GuildRepository;
import com.jftse.server.core.service.GuildService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class GuildServiceImpl implements GuildService {
    private final GuildRepository guildRepository;

    @Override
    public Guild save(Guild guild) {
        return guildRepository.save(guild);
    }

    @Override
    public void remove(Long guildId) {
        guildRepository.deleteById(guildId);
    }

    @Override
    public Guild findByName(String name) {
        List<Guild> guildList = guildRepository.findAllByName(name);
        return guildList.size() != 0 ? guildList.get(0) : null;
    }

    @Override
    public List<Guild> findAllByNameContaining(String name) {
        return guildRepository.findAllByNameContainingIgnoreCase(name);
    }

    @Override
    public Guild findById(Long id) { return guildRepository.findById(id).orElse(null); }

    @Override
    public List<Guild> findAll() { return guildRepository.findAll(); }
}
