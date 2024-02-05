package com.jftse.entities.database.model.pocket;

import com.jftse.entities.database.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;

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

    @Column(columnDefinition = "int default 0")
    private Integer enchantStr = 0;
    @Column(columnDefinition = "int default 0")
    private Integer enchantSta = 0;
    @Column(columnDefinition = "int default 0")
    private Integer enchantDex = 0;
    @Column(columnDefinition = "int default 0")
    private Integer enchantWil = 0;

    @Column(columnDefinition = "int default 0")
    private Integer enchantElement = 0;
    @Column(columnDefinition = "int default 0")
    private Integer enchantLevel = 0;
}
