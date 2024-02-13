package com.jftse.server.core.matchplay;

import com.jftse.server.core.item.EElementalProperty;

public class WaterElement extends BaseElement {
    public WaterElement(double minEfficiency, double maxEfficiency) {
        super(EElementalProperty.WATER, minEfficiency, maxEfficiency);
    }

    @Override
    public boolean isWeakAgainst(Elementable elementable) {
        return elementable instanceof EarthElement;
    }

    @Override
    public boolean isResistantTo(Elementable elementable) {
        return elementable instanceof WindElement;
    }

    @Override
    public boolean isStrongAgainst(Elementable elementable) {
        return elementable instanceof FireElement;
    }
}
