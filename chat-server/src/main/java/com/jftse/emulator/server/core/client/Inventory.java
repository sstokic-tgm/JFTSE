package com.jftse.emulator.server.core.client;

public interface Inventory {
    boolean addItem(int productIndex, int quantity);
    boolean addItem(int itemIndex, String category, int quantity);
    boolean removeItem(int itemIndex, String category, int quantity);
    boolean removeItem(Long id, int quantity);
    boolean removeItem(Long id);
}
