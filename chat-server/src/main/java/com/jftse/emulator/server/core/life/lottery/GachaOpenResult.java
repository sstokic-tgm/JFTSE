package com.jftse.emulator.server.core.life.lottery;

import com.jftse.entities.database.model.pocket.PlayerPocket;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GachaOpenResult {
    private boolean success;

    private boolean consumedGachaRemoved;
    private PlayerPocket consumedGachaPocket;

    private PlayerPocket awardedItem;
    private boolean duplicateConverted;

    private int gachaTokensAdded;

    private int pityBefore;
    private int pityAfter;
    private boolean pityGuaranteed;
    private boolean rareItemHit;

    private String failureReason;
}
