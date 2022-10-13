package com.jftse.server.core.service;

import com.jftse.entities.database.model.battle.WillDamage;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

public interface WillDamageService {
    @PostConstruct
    void init() throws IOException;

    List<WillDamage> getWillDamages();
}
