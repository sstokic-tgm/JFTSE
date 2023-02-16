package com.jftse.emulator.server.core.service;

import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.guild.GuildMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class GuildMemberService {
    private final GuildMemberRepository guildMemberRepository;

    public GuildMember findById(Long id) {
        return guildMemberRepository.findById(id).orElse(null);
    }

    public GuildMember save(GuildMember guildMember) {
        return guildMemberRepository.save(guildMember);
    }

    public GuildMember getByPlayer(Player player) {
        return guildMemberRepository.findByPlayer(player).orElse(null);
    }
}
