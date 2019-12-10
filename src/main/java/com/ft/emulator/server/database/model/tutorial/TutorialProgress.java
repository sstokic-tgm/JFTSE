package com.ft.emulator.server.database.model.tutorial;

import com.ft.emulator.common.model.AbstractBaseModel;
import com.ft.emulator.server.database.model.character.CharacterPlayer;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Getter
@Setter
@Audited
@Entity
public class TutorialProgress extends AbstractBaseModel {

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH, optional = false)
    private CharacterPlayer characterPlayer;

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH, optional = false)
    private Tutorial tutorial;

    // char
    private Integer success;
    // char
    private Integer attempts;
}