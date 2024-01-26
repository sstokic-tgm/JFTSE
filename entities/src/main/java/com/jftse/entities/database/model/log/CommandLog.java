package com.jftse.entities.database.model.log;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.player.Player;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class CommandLog extends AbstractBaseModel {
    private String command;
    private String arguments;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    @JoinColumn(name = "player_id", referencedColumnName = "id")
    private Player player;
}
