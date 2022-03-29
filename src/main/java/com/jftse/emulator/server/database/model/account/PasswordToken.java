package com.jftse.emulator.server.database.model.account;

import com.jftse.emulator.common.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
public class PasswordToken extends AbstractIdBaseModel {
    private Date created;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;

    private String resetToken;
    private Integer validFor;

    @PrePersist
    @PreUpdate
    protected void prePersist() {
        if (this.created == null)
            this.created = new Date();
    }
}
