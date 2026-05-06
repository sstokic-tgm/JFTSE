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
public class GuildMemberServiceImpl implements GuildMemberService {
    private final GuildMemberRepository guildMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public GuildMember findById(Long id) {
        return guildMemberRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public GuildMember save(GuildMember guildMember) {
        return guildMemberRepository.save(guildMember);
    }

    @Override
    @Transactional(readOnly = true)
    public GuildMember getByPlayer(Player player) {
        return guildMemberRepository.findByPlayer(player).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public GuildMember getByPlayer(Long playerId) {
        return guildMemberRepository.findByPlayerId(playerId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWaitingForApproval(Long playerId) {
        return guildMemberRepository.findByPlayerIdAndWaitingForApprovalTrue(playerId).isPresent();
    }
}
