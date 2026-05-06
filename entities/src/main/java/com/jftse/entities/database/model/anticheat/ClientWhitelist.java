package com.jftse.entities.database.model.anticheat;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.account.Account;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(indexes = {
        @Index(name = "idx_cwlist_account_id", columnList = "account_id"),
        @Index(name = "idx_cwlist_ip_hwid", columnList = "ip, hwid"),
        @Index(name = "idx_cwlist_hwid_flagged", columnList = "hwid, flagged"),
        @Index(name = "idx_cwlist_ip_hwid_createdt", columnList = "ip, hwid, created"),
        @Index(name = "idx_cwlist_hwid_flagged_createdt", columnList = "hwid, flagged, created")
}
)
public class ClientWhitelist extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;

    private String ip;
    private Integer port;
    private Boolean flagged;
    private Boolean isAuthenticated;
    private String hwid;
    private Boolean isActive;
}