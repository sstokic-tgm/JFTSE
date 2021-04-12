package com.jftse.emulator.server.database.model.anticheat;

import com.jftse.emulator.common.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class ClientWhitelist extends AbstractBaseModel {
    private String ip;
    private Integer port;
    private Boolean flagged;
    private Boolean isAuthenticated;
    private String hwid;
}