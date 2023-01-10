package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.guild.GuildMemberRepository;
import com.jftse.server.core.service.GuildMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class GuildMemberServiceImpl implements GuildMemberService {
    private final GuildMemberRepository guildMemberRepository;

    @Override
    public GuildMember findById(Long id) {
        return guildMemberRepository.findById(id).orElse(null);
    }

    @Override
    public GuildMember save(GuildMember guildMember) {
        return guildMemberRepository.save(guildMember);
    }

    @Override
    public GuildMember getByPlayer(Player player) {
        return guildMemberRepository.findByPlayer(player).orElse(null);
    }
}
