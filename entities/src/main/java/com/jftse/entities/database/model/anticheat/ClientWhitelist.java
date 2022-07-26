package com.jftse.entities.database.model.anticheat;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.account.Account;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class ClientWhitelist extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;

    private String ip;
    private Integer port;
    private Boolean flagged;
    private Boolean isAuthenticated;
    private String hwid;
    private Boolean isActive;
}