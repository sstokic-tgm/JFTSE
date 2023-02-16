package com.jftse.entities.database.model.challenge;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.player.Player;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;

@Getter
@Setter
@Audited
@Entity
public class ChallengeProgress extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "player_id", referencedColumnName = "id")
    private Player player;

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH, optional = false)
    private Challenge challenge;

    // char
    private Integer success;
    //char
    private Integer attempts;
}
