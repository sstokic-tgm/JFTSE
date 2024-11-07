package com.jftse.entities.database.model.account;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.ServerType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

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
    private ServerType loggedInServer;
    private ServerType logoutServer;
    private Long lastSelectedPlayerId;

    @Column(nullable = false, columnDefinition = "bit(1) DEFAULT 1")
    private Boolean active = true;

    private Boolean banned = false;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "account_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Set<Role> roles;

    @Column(nullable = false, columnDefinition = "int(11) DEFAULT 0")
    private Integer verifyLevel = 0;

    private String discordId;

    private String agreements;
}
