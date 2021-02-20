package com.jftse.emulator.server.database.model.home;

import com.jftse.emulator.common.model.AbstractBaseModel;
import com.jftse.emulator.server.database.model.account.Account;
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
public class AccountHome extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH, optional = false)
    private Account account;

    private Byte level = 1;

    private Integer housingPoints = 0;
    private Integer famousPoints = 0;
    private Integer furnitureCount = 0;

    private Byte basicBonusExp = 0;
    private Byte basicBonusGold = 0;
    private Byte battleBonusExp = 0;
    private Byte battleBonusGold = 0;
}
