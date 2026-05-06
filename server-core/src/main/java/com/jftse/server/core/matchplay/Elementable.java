package com.jftse.server.core.matchplay;

import com.jftse.server.core.item.EElementalProperty;

public interface Elementable {
    EElementalProperty getProperty();
    boolean isWeakAgainst(Elementable elementable);
    boolean isResistantTo(Elementable elementable);
    boolean isStrongAgainst(Elementable elementable);
    double getEfficiency();
}
