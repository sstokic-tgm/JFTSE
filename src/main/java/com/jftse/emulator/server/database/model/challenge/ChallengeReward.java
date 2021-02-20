package com.jftse.emulator.server.database.model.challenge;

import com.jftse.emulator.common.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.MappedSuperclass;

@Getter
@Setter
@MappedSuperclass
public class ChallengeReward extends AbstractIdBaseModel {
    private Boolean itemRewardRepeat;

    private Integer quantityMin1;
    private Integer quantityMin2;
    private Integer quantityMin3;

    private Integer quantityMax1;
    private Integer quantityMax2;
    private Integer quantityMax3;

    private Byte rewardExp;
    private Integer rewardGold;
    private Integer rewardItem1;
    private Integer rewardItem2;
    private Integer rewardItem3;
}
