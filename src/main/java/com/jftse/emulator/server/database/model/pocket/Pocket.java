package com.jftse.emulator.server.database.model.pocket;

import com.jftse.emulator.common.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;

@Getter
@Setter
@Audited
@Entity
public class Pocket extends AbstractBaseModel {
    private Integer belongings = 0;
    private Integer maxBelongings = 150;
}
