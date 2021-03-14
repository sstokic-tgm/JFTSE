package com.jftse.emulator.server.database.repository.guild;

import com.jftse.emulator.server.database.model.guild.GuildMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuildMemberRepository extends JpaRepository<GuildMember, Long> {
}
