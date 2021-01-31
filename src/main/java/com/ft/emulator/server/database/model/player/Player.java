package com.ft.emulator.server.database.model.player;

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
public class Player extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH, optional = false)
    private Pocket pocket;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "clothEquipment_id", referencedColumnName = "id")
    private ClothEquipment clothEquipment;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "quickSlotEquipment_id", referencedColumnName = "id")
    private QuickSlotEquipment quickSlotEquipment;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "playerStatistic_id", referencedColumnName = "id")
    private PlayerStatistic playerStatistic;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "player")
    private List<ChallengeProgress> challengeProgressList;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "player")
    private List<TutorialProgress> tutorialProgressList;

    private Boolean firstPlayer = false;
    private Boolean alreadyCreated = false;
    private String name = "";
    private Byte level = 1;
    private Integer expPoints = 0;
    private Boolean nameChangeAllowed = false;
    private Integer gold = 0;
    private Byte playerType;

    private Byte strength = 0;
    private Byte stamina = 0;
    private Byte dexterity = 0;
    private Byte willpower = 0;
    private Byte statusPoints = 0;
}
