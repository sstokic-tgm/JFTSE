package com.jftse.server.core.service;

import com.jftse.server.core.matchplay.battle.GuardianBtItemList;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

public interface GuardianSkillsService {
    @PostConstruct
    void init() throws IOException;

    GuardianBtItemList findGuardianBtItemListById(int btItemId);

    List<GuardianBtItemList> getGuardianSkills();
}
