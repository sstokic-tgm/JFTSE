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
public class QuickSlotEquipment extends AbstractBaseModel {
    private Integer slot1 = 0;
    private Integer slot2 = 0;
    private Integer slot3 = 0;
    private Integer slot4 = 0;
    private Integer slot5 = 0;
}
