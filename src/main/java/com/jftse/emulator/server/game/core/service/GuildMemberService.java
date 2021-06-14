package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.repository.guild.GuildMemberRepository;
import com.jftse.emulator.server.database.repository.player.PlayerRepository;
import com.jftse.emulator.server.game.core.packet.packets.player.C2SPlayerCreatePacket;
import com.jftse.emulator.server.game.core.packet.packets.player.C2SPlayerStatusPointChangePacket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class GuildMemberService {
    private final GuildMemberRepository guildMemberRepository;

    public GuildMember save(GuildMember guildMember) {
        return guildMemberRepository.save(guildMember);
    }

    public GuildMember getByPlayer(Player player) {
        return guildMemberRepository.findByPlayer(player).orElse(null);
    }
}
