package com.ft.emulator.server.database.model.item;

import com.ft.emulator.common.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@Getter
@Setter
@MappedSuperclass
public class Item extends AbstractIdBaseModel {
    @Column(unique = true)
    private Integer itemIndex;

    private String name;
}
