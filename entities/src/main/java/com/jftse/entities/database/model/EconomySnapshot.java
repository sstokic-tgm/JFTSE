package com.jftse.entities.database.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "economy_snapshots")
public class EconomySnapshot extends AbstractBaseModel {
    private LocalDateTime dateTime;

    private Long totalAp;
    private Long totalGold;

    private Integer activePlayers;
    private Long avgApPerAccount;
    private Long avgGoldPerPlayer;
}
