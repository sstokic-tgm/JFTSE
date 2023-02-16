package com.jftse.server.core.service;

import java.util.List;

public interface ItemMaterialService {
    List<Integer> findAllItemIndexesDB();

    List<Integer> findAllItemIndexes();
}
