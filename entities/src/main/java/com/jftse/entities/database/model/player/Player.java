package com.jftse.entities.database.model.player;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.challenge.ChallengeProgress;
import com.jftse.entities.database.model.pet.Pet;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.entities.database.model.tutorial.TutorialProgress;
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
public class Player extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pocket_id", referencedColumnName = "id", unique = true)
    private Pocket pocket;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothEquipment_id", referencedColumnName = "id", unique = true)
    private ClothEquipment clothEquipment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quickSlotEquipment_id", referencedColumnName = "id", unique = true)
    private QuickSlotEquipment quickSlotEquipment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toolSlotEquipment_id", referencedColumnName = "id", unique = true)
    private ToolSlotEquipment toolSlotEquipment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialSlotEquipment_id", referencedColumnName = "id", unique = true)
    private SpecialSlotEquipment specialSlotEquipment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cardSlotEquipment_id", referencedColumnName = "id", unique = true)
    private CardSlotEquipment cardSlotEquipment;

    /* @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "battlemonSlotEquipment_id", referencedColumnName = "id")
    private BattlemonSlotEquipment battlemonSlotEquipment;
     */

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerStatistic_id", referencedColumnName = "id", unique = true)
    private PlayerStatistic playerStatistic;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "player")
    private List<ChallengeProgress> challengeProgressList;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "player")
    private List<TutorialProgress> tutorialProgressList;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "player")
    private List<Pet> petList;

    private Boolean firstPlayer = false;
    private Boolean alreadyCreated = false;
    private String name = "";
    private Byte level = 1;
    private Integer expPoints = 0;
    private Boolean nameChangeAllowed = false;
    private Date lastNameChangeDate;
    private Integer gold = 0;
    private Integer couplePoints = 0;
    private Byte playerType;

    private Byte strength = 0;
    private Byte stamina = 0;
    private Byte dexterity = 0;
    private Byte willpower = 0;
    private Byte statusPoints = 0;

    @Column(columnDefinition = "bit(1) DEFAULT 0")
    private Boolean online = false;
}
