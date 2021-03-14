package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.server.database.model.guild.Guild;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.repository.guild.GuildMemberRepository;
import com.jftse.emulator.server.database.repository.guild.GuildRepository;
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

    public void remove(Long guildMemberId) {
        guildRepository.deleteById(guildMemberId);
    }

    public Guild findByName(String name) {
        List<Guild> guildList = guildRepository.findAllByName(name);
        return guildList.size() != 0 ? guildList.get(0) : null;
    }

    public Guild findById(Long id) { return guildRepository.findById(id).get(); }

    public List<Guild> findAll() { return guildRepository.findAll(); }
}
