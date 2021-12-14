package com.jftse.emulator.server.core.matchplay.battle;

import com.jftse.emulator.server.core.packet.packets.matchplay.C2SMatchplayUsesSkill;
import com.jftse.emulator.server.database.model.battle.Skill;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class SkillUse {
    private Skill skill;
    private byte attackerPosition;
    private byte targetPosition;
    private boolean isQuickSlot;
    private long timestamp;
    private AtomicBoolean used;
    private AtomicInteger shotCount;
    private AtomicBoolean playTimePassed = new AtomicBoolean(false);

    private AtomicBoolean spiderMineIsPlaced = new AtomicBoolean(false);
    private AtomicBoolean spiderMineIsExploded = new AtomicBoolean(false);
    private int spiderMineId;
    private int spiderMineEffectId;

    public SkillUse(Skill skill, byte attackerPosition, byte targetPosition, boolean isQuickSlot, long timestamp, boolean used) {
        this.skill = skill;
        this.attackerPosition = attackerPosition;
        this.targetPosition = targetPosition;
        this.isQuickSlot = isQuickSlot;
        this.timestamp = timestamp;
        this.used = new AtomicBoolean(used);
        this.shotCount = new AtomicInteger(1);
    }

    public SkillUse(Skill skill, C2SMatchplayUsesSkill usesSkill, long timestamp, boolean used) {
        this.skill = skill;
        this.attackerPosition = usesSkill.getAttackerPosition();
        this.targetPosition = usesSkill.getTargetPosition();
        this.isQuickSlot = usesSkill.isQuickSlot();
        this.timestamp = timestamp;
        this.used = new AtomicBoolean(used);
        this.shotCount = new AtomicInteger(1);
    }

    public boolean isSame(SkillUse other) {
        boolean sameAttackerPos = this.getAttackerPosition() == other.getAttackerPosition();
        boolean sameTargetPos = this.getTargetPosition() == other.getTargetPosition();
        boolean sameSkill = this.skill.getId().equals(other.getSkill().getId());
        return sameAttackerPos && sameTargetPos && sameSkill;
    }

    public boolean isAttackerGod(short attackerPosition) {
        return attackerPosition == 4;
    }

    public boolean isAttackerPlayer(short attackerPosition) {
        return attackerPosition < 4;
    }

    public boolean isAttackerGuardian(short attackerPosition) {
        return attackerPosition > 9;
    }

    public boolean isTargetPlayer(short targetPosition) {
        return targetPosition < 4;
    }

    public boolean isTargetGuardian(short targetPosition) {
        return targetPosition > 9;
    }

    public boolean isAttackerPositionGod() {
        return attackerPosition == 4;
    }

    public boolean isAttackerPositionPlayer() {
        return attackerPosition < 4;
    }

    public boolean isAttackerPositionGuardian() {
        return attackerPosition > 9;
    }

    public boolean isTargetPositionPlayer() {
        return attackerPosition < 4;
    }

    public boolean isTargetPositionGuardian() {
        return attackerPosition > 9;
    }

    public void setShotCountByDex(int dex) {
        int shotCount;
        switch (skill.getId().intValue()) {
            case 3, 4, 6 -> { // BigMeteo, SmallMeteo, HomingBall
                shotCount = skill.getShotCnt();
                if (dex > 29 && dex < 40)
                    shotCount += 1;
                else if (dex > 39 && dex < 50)
                    shotCount += 2;
                else if (dex > 49 && dex < 60)
                    shotCount += 3;
                else if (dex > 59)
                    shotCount += 4;
            }
            default -> shotCount = skill.getShotCnt(); // all other
        }
        this.shotCount.set(shotCount);
    }

    public boolean isShield() {
        return skill.getId().equals(10L);
    }

    public boolean isHeal() {
        return skill.getId().equals(1L) || skill.getId().equals(2L) || skill.getId().equals(31L) || skill.getId().equals(39L);
    }

    public boolean isRangeHeal() {
        return skill.getId().equals(16L) || skill.getId().equals(17L) || skill.getId().equals(18L) || skill.getId().equals(19L);
    }

    public boolean isRangeShield() {
        return skill.getId().equals(20L);
    }

    public boolean isSpiderMine() {
        return skill.getId().equals(12L);
    }

    public boolean isMiniam() {
        return skill.getId().equals(9L);
    }

    public boolean isApollonFlash() {
        return skill.getId().equals(11L);
    }

    public boolean isEarth() {
        return skill.getId().equals(23L);
    }
}
