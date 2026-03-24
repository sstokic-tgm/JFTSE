package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.repository.guild.GuildMemberRepository;
import com.jftse.entities.database.repository.guild.GuildRepository;
import com.jftse.server.core.service.GuildService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GuildServiceImpl implements GuildService {
    private final GuildRepository guildRepository;
    private final GuildMemberRepository guildMemberRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Guild save(Guild guild) {
        return guildRepository.save(guild);
    }

    @Override
    @Transactional
    public void remove(Long guildId) {
        guildRepository.deleteById(guildId);
    }

    @Override
    @Transactional(readOnly = true)
    public Guild findByName(String name) {
        List<Guild> guildList = guildRepository.findAllByName(name);
        return !guildList.isEmpty() ? guildList.getFirst() : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Guild> findAllByNameContaining(String name) {
        return guildRepository.findAllByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Guild findById(Long id) { return guildRepository.findById(id).orElse(null); }

    @Override
    @Transactional(readOnly = true)
    public Guild findWithMembersById(Long id) {
        return guildRepository.findWithMembersById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Guild findWithMembersByPlayerId(Long playerId) {
        GuildMember gm = guildMemberRepository.findByPlayerId(playerId).orElse(null);
        if (gm != null) {
            return findWithMembersById(gm.getGuild().getId());
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Guild> findAll() { return guildRepository.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public List<Guild> findAll(int offset) {
        Long lastId = offset > 0 ? (long) offset : 0L;

        List<Long> ids = guildRepository.findIdsByIdGreaterThanOrderByIdAsc(lastId, PageRequest.of(0, 10));
        if (ids.isEmpty())
            return List.of();

        return guildRepository.findWithMembersByIdIn(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Guild> findAllGuildLeagues(int page) {
        int pageIndex = Math.max(page - 1, 0);
        return guildRepository.findAllByOrderByLeaguePointsDescIdAsc(PageRequest.of(pageIndex, 10));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Guild changeInformation(Long guildId, String introduction, byte minLevel, boolean isPublic, Byte[] allowedCharacterTypes) {
        Optional<Guild> guildOptional = guildRepository.findWithMembersById(guildId);
        if (guildOptional.isPresent()) {
            Guild guild = guildOptional.get();
            guild.setIntroduction(introduction);
            guild.setLevelRestriction(minLevel);
            guild.setIsPublic(isPublic);
            guild.setAllowedCharacterType(allowedCharacterTypes);
            return guildRepository.save(guild);
        } else {
            return null;
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void changeReverseMemberStatus(Long guildId, int playerId, boolean isApproved) {
        Optional<Guild> guildOptional = guildRepository.findWithMembersById(guildId);
        if (guildOptional.isPresent()) {
            Guild guild = guildOptional.get();
            GuildMember reverseMember = guild.getMemberList().stream()
                    .filter(gm -> gm.getPlayer().getId() == playerId)
                    .findFirst()
                    .orElse(null);

            if (reverseMember != null) {
                if (isApproved) {
                    reverseMember.setWaitingForApproval(false);
                } else {
                    guild.getMemberList().removeIf(x -> x.getId().equals(reverseMember.getId()));
                }
                guildRepository.save(guild);
            }
        }
    }
}
