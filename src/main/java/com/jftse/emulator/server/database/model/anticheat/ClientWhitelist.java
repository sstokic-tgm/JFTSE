package com.jftse.emulator.server.database.model.anticheat;

import com.jftse.emulator.common.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;

@Getter
@Setter
@Audited
@Entity
public class ClientWhitelist extends AbstractBaseModel {
    private String ip;
    private Integer port;
    private Boolean flagged;
}