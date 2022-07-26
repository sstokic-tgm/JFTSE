package com.jftse.emulator.server.core.service;

import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.repository.guild.GuildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class GuildService {
    private final GuildRepository guildRepository;

    public Guild save(Guild guild) {
        return guildRepository.save(guild);
    }

    public void remove(Long guildId) {
        guildRepository.deleteById(guildId);
    }

    public Guild findByName(String name) {
        List<Guild> guildList = guildRepository.findAllByName(name);
        return guildList.size() != 0 ? guildList.get(0) : null;
    }

    public List<Guild> findAllByNameContaining(String name) {
        return guildRepository.findAllByNameContainingIgnoreCase(name);
    }

    public Guild findById(Long id) { return guildRepository.findById(id).orElse(null); }

    public List<Guild> findAll() { return guildRepository.findAll(); }
}
