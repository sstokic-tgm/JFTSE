package com.jftse.emulator.server.database.model.pet;

import com.jftse.emulator.common.model.AbstractBaseModel;
import com.jftse.emulator.server.database.model.pet.PetStatistic;
import com.jftse.emulator.server.database.model.player.Player;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Audited
@Entity
public class Pet extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "petStatistic_id", referencedColumnName = "id")
    private PetStatistic petStatistic;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "player_id", referencedColumnName = "id")
    private Player player;
    private Byte type;
    private String name;
    private Byte level;
    private Integer expPoints;
    private Integer hp;
    private Byte strength;
    private Byte stamina;
    private Byte dexterity;
    private Byte willpower;
    private Integer hunger;
    private Integer energy;
    private Integer lifeMax;
    private Date validUntil;
    private Byte alive;
}
