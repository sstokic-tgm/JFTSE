package com.jftse.emulator.server.database.model.tutorial;

import com.jftse.emulator.common.model.AbstractBaseModel;
import com.jftse.emulator.server.database.model.player.Player;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;

@Getter
@Setter
@Audited
@Entity
public class TutorialProgress extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "player_id", referencedColumnName = "id")
    private Player player;

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH, optional = false)
    private Tutorial tutorial;

    // char
    private Integer success;
    // char
    private Integer attempts;
}
