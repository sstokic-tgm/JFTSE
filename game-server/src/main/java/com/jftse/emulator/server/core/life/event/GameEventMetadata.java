package com.jftse.emulator.server.core.life.event;

import com.jftse.emulator.server.core.interaction.GameEventScriptable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameEventMetadata {
    private Long id;
    private String name;
    private String type;
    private String desc;
    private boolean enabled;
    private GameEventScriptable event;

    public GameEventMetadata(Long id, String name, String type, String desc, boolean enabled, GameEventScriptable event) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.desc = desc;
        this.enabled = enabled;
        this.event = event;
    }

    public GameEventMetadata(Long id, String name, String type, String desc, GameEventScriptable event) {
        this(id, name, type, desc, false, event);
    }
}
