package com.jftse.server.core.matchplay;

public interface Elementable {
    boolean isWeakAgainst(Elementable elementable);
    boolean isResistantTo(Elementable elementable);
    boolean isStrongAgainst(Elementable elementable);
    double getEfficiency();
}
