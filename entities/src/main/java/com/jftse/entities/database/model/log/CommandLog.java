package com.jftse.entities.database.model.log;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.player.Player;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Getter
@Setter
@Entity
public class CommandLog extends AbstractBaseModel {
    private String command;
    private String arguments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", referencedColumnName = "id")
    private Player player;
}
