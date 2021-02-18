package com.jftse.emulator.server.database.model.pocket;

import com.jftse.emulator.common.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Getter
@Setter
@Audited
@Entity
public class PlayerPocket extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH, optional = false)
    private Pocket pocket;

    private String category;
    private Integer itemIndex;
    private String useType;
    private Integer itemCount;
}
