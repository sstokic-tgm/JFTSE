package com.ft.emulator.server.database.model.home;

import com.ft.emulator.common.model.AbstractBaseModel;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Getter
@Setter
@Audited
@Entity
public class HomeInventory extends AbstractBaseModel {

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH, optional = false)
    private AccountHome accountHome;

    private Long itemIndex;

    private Byte unk0;
    private Byte unk1;
    private Byte xPos;
    private Byte yPos;
}