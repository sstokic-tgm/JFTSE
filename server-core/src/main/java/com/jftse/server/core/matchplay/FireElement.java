package com.jftse.server.core.matchplay;

import com.jftse.server.core.item.EElementalProperty;

public class FireElement extends BaseElement {
    public FireElement(double minEfficiency, double maxEfficiency) {
        super(EElementalProperty.FIRE, minEfficiency, maxEfficiency);
    }

    @Override
    public boolean isWeakAgainst(Elementable elementable) {
        return elementable instanceof WaterElement;
    }

    @Override
    public boolean isResistantTo(Elementable elementable) {
        return elementable instanceof EarthElement;
    }

    @Override
    public boolean isStrongAgainst(Elementable elementable) {
        return elementable instanceof WindElement;
    }
}
