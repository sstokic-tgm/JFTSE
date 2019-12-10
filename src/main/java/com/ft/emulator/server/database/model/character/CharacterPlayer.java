package com.ft.emulator.server.database.model.character;

import com.ft.emulator.common.model.AbstractBaseModel;
import com.ft.emulator.server.database.model.account.Account;
import com.ft.emulator.server.database.model.challenge.ChallengeProgress;
import com.ft.emulator.server.database.model.pocket.Pocket;
import com.ft.emulator.server.database.model.tutorial.TutorialProgress;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Audited
@Entity
public class CharacterPlayer extends AbstractBaseModel {

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH, optional = false)
    private Account account;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "characterPlayer")
    private List<ChallengeProgress> challengeProgresses;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "characterPlayer")
    private List<TutorialProgress> tutorialProgresses;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH, optional = true)
    private Pocket pocket;

    // general
    private Boolean firstCharacter = false;
    private Boolean alreadyCreated = false;
    private String name = "";
    private Byte level = 1;
    private Integer expPoints = 0;
    private Boolean nameChangeAllowed = false;
    private Integer gold = 0;
    private Byte cType = 1; // forCharacter type

    private Byte strength = 0;
    private Byte stamina = 0;
    private Byte dexterity = 0;
    private Byte willpower = 0;
    private Byte statusPoints = 0;


    // stats
    private Integer battlesLost = 0;
    private Integer battlesWon = 0;

    // items
    private Integer bag = 0;
    private Integer dress = 0;
    private Integer dye = 0;
    private Integer face = 0;
    private Integer glasses = 0;
    private Integer gloves = 0;
    private Integer hair = 0;
    private Integer hat = 0;
    private Integer pants = 0;
    private Integer racket = 0;
    private Integer shoes = 0;
    private Integer socks = 0;
}