package com.jftse.emulator.server.database.model.auth;

import com.jftse.emulator.common.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class AuthToken extends AbstractIdBaseModel {
    private String token;
    private Long loginTimestamp;
    private String accountName;
}
