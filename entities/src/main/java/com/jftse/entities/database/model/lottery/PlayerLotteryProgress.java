package com.jftse.entities.database.model.lottery;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.player.Player;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(indexes = {
        @Index(name = "idx_pgp_playerid_gachaindex", columnList = "player_id, gacha_index")
},
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"player_id", "gacha_index"})
        })
public class PlayerLotteryProgress extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false, updatable = false)
    private Player player;

    @Column(name = "gacha_index", nullable = false, updatable = false)
    private int gachaIndex;

    @Column(columnDefinition = "int default 0")
    private int pityCount = 0;

    @Column(columnDefinition = "bigint default 0")
    private long lifetimePullCount = 0;

    private Instant lastPullAt;
    private Instant lastResetAt;
}
