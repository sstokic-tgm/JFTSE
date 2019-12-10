package com.ft.emulator.server.database.model.item;

import com.ft.emulator.common.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@Getter
@Setter
@MappedSuperclass
public class Item extends AbstractBaseModel {

    @Column(unique = true)
    private Long itemIndex;

    private String name;
}