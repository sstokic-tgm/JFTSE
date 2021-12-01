package com.jftse.emulator.server.database.model.guild;

import com.jftse.emulator.common.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.Audited;

import java.util.List;

import javax.persistence.*;

@Getter
@Setter
@Audited
@Entity
public class Guild extends AbstractBaseModel {
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "guild")
    private List<GuildMember> memberList;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "guild")
    private List<GuildGoldUsage> goldUsageList;

    @Column(unique = true)
    private String name;
    private String introduction;
    private String notice = "";
    private Integer logoBackgroundId;
    private Integer logoBackgroundColor = -1;
    private Integer logoPatternId = -1;
    private Integer logoPatternColor = -1;
    private Integer logoMarkId = -1;
    private Integer logoMarkColor = -1;
    private Byte maxMemberCount = 25;
    private Byte level = 1;
    private Integer clubPoints = 0;
    private Integer leaguePoints = 0;
    private Integer gold = 0;
    private Integer battleRecordWin = 0;
    private Integer battleRecordLoose = 0;
    private Integer leagueRecordWin = 0;
    private Integer leagueRecordLoose = 0;
    private Byte levelRestriction = 1;
    private Boolean isPublic = true;
    private Byte[] allowedCharacterType;
    private Boolean castleOwner = false;
}