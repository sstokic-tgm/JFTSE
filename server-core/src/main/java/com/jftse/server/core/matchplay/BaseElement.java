package com.jftse.server.core.matchplay;

import com.jftse.server.core.item.EElementalProperty;

import java.util.Random;

public abstract class BaseElement implements Elementable {
    private final EElementalProperty property;
    private final double minEfficiency;
    private final double maxEfficiency;

    private final Random random;

    public BaseElement(EElementalProperty property, double minEfficiency, double maxEfficiency) {
        this.property = property;
        this.minEfficiency = minEfficiency;
        this.maxEfficiency = maxEfficiency;
        this.random = new Random();
    }

    public EElementalProperty getProperty() {
        return property;
    }

    @Override
    public double getEfficiency() {
        return minEfficiency + (maxEfficiency - minEfficiency) * random.nextDouble();
    }
}
