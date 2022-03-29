package com.jftse.emulator.server.database.model.account;

import com.jftse.emulator.common.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Audited
@Entity
public class Account extends AbstractBaseModel {
    private Integer ap;
    private Date lastLogin;

    @Column(unique = true)
    private String username;
    private String password;
    private String email;
    // char
    private Integer status;
    private Boolean gameMaster;

    private String banReason;
    private Date bannedUntil;
}
