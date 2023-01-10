package com.jftse.entities.database.model.log;

import com.jftse.entities.database.model.AbstractBaseModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
@AllArgsConstructor
@RequiredArgsConstructor
public class BlockedIP extends AbstractBaseModel {
    private String ip;
    private ServerType serverType;
}
