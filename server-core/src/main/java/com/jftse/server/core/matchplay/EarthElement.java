package com.jftse.server.core.matchplay;

import com.jftse.server.core.item.EElementalProperty;

public class EarthElement extends BaseElement {


    public EarthElement(double minEfficiency, double maxEfficiency) {
        super(EElementalProperty.EARTH, minEfficiency, maxEfficiency);
    }

    @Override
    public boolean isWeakAgainst(Elementable elementable) {
        return elementable instanceof WindElement;
    }

    @Override
    public boolean isResistantTo(Elementable elementable) {
        return elementable instanceof FireElement;
    }

    @Override
    public boolean isStrongAgainst(Elementable elementable) {
        return elementable instanceof WaterElement;
    }

}
