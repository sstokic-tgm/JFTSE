package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.shared.packets.matchplay.CMSGSpellHitsTarget;
import lombok.Getter;

public class CMSGSpellHitsTargetExtended {
    @Getter
    private final CMSGSpellHitsTarget delegate;

    public CMSGSpellHitsTargetExtended(CMSGSpellHitsTarget delegate) {
        this.delegate = delegate;
    }

    public boolean getApplySkillEffect() {
        return delegate.getApplySkillEffect() == 0;
    }

    public short getAttackerPosition() {
        return delegate.getAttackerPosition();
    }

    public byte getUnk0() {
        return delegate.getUnk0();
    }

    public short getTargetPosition() {
        return delegate.getTargetPosition();
    }

    public byte getUnk1() {
        return delegate.getUnk1();
    }

    public byte getSkillId() {
        return delegate.getSkillId();
    }

    public byte getAttackerBuffId1() {
        return delegate.getAttackerBuffId1();
    }

    public byte getAttackerBuffId2() {
        return delegate.getAttackerBuffId2();
    }

    public byte getUnk3() {
        return delegate.getUnk3();
    }

    public byte getReceiverBuffId1() {
        return delegate.getReceiverBuffId1();
    }

    public byte getUnk4() {
        return delegate.getUnk4();
    }

    public byte getReceiverBuffId2() {
        return delegate.getReceiverBuffId2();
    }

    public byte getDamageType() {
        return delegate.getDamageType();
    }

    public byte getUnk6() {
        return delegate.getUnk6();
    }

    public byte getUnk7() {
        return delegate.getUnk7();
    }

    public int getXKnockbackPosition() {
        return delegate.getXKnockbackPosition();
    }

    public int getYKnockbackPosition() {
        return delegate.getYKnockbackPosition();
    }

    public byte getUnk8() {
        return delegate.getUnk8();
    }
}
