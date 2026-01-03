package com.jftse.server.core;

public interface ServerLoopMetrics {
    long getTicks();
    double getAvgTickMs();
    long getMaxTickMs();
    double getAvgUpdateMs();
    long getMaxUpdateMs();
}
