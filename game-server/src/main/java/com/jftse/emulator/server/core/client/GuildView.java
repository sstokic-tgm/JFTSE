package com.jftse.emulator.server.core.client;

import com.jftse.entities.database.model.guild.Guild;

public record GuildView(long id, String name, int logoBackgroundId, int logoBackgroundColor, int logoPatternId,
                        int logoPatternColor, int logoMarkId, int logoMarkColor) {
    public static GuildView fromEntity(Guild guild) {
        return new GuildView(
                guild.getId(),
                guild.getName(),
                guild.getLogoBackgroundId(),
                guild.getLogoBackgroundColor(),
                guild.getLogoPatternId(),
                guild.getLogoPatternColor(),
                guild.getLogoMarkId(),
                guild.getLogoMarkColor()
        );
    }
}
