package com.jftse.emulator.server.database.model.account;

import com.jftse.emulator.common.model.AbstractBaseModel;
import com.jftse.emulator.server.database.model.player.Player;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Audited
@Entity
public class Account extends AbstractBaseModel {
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "account")
    private List<Player> playerList;

    private Integer ap;
    private Date lastLogin;

    @Column(unique = true)
    private String username;
    private String password;
    // char
    private Integer status;
    private Boolean gameMaster;
}
