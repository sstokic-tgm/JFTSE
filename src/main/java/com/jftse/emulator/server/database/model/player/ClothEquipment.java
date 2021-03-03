package com.jftse.emulator.server.database.model.player;

import com.jftse.emulator.common.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;

@Getter
@Setter
@Audited
@Entity
public class ClothEquipment extends AbstractBaseModel {
    private Integer bag = 0;
    private Integer dress = 0;
    private Integer dye = 0;
    private Integer face = 0;
    private Integer glasses = 0;
    private Integer gloves = 0;
    private Integer hair = 0;
    private Integer hat = 0;
    private Integer pants = 0;
    private Integer racket = 0;
    private Integer shoes = 0;
    private Integer socks = 0;
}
