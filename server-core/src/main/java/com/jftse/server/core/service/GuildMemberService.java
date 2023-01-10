package com.jftse.server.core.service;

import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;

public interface GuildMemberService {
    GuildMember findById(Long id);

    GuildMember save(GuildMember guildMember);

    GuildMember getByPlayer(Player player);
}
