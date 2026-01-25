package com.jftse.entities.database.model.home;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.account.Account;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Audited
@Entity
public class AccountHome extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Account account;

    private Byte level = 1;

    private Integer housingPoints = 0;
    private Integer famousPoints = 0;
    private Integer furnitureCount = 0;

    private Byte basicBonusExp = 0;
    private Byte basicBonusGold = 0;
    private Byte battleBonusExp = 0;
    private Byte battleBonusGold = 0;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "accountHome")
    private List<HomeInventory> inventoryItems;
}
