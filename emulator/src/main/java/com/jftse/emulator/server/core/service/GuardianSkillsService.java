package com.jftse.emulator.server.core.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jftse.emulator.common.utilities.ResourceUtil;
import com.jftse.emulator.server.core.matchplay.battle.GuardianBtItemList;
import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

@Service
@Scope("singleton")
@Getter
public class GuardianSkillsService {
    private List<GuardianBtItemList> guardianSkills;

    @PostConstruct
    public void init() throws IOException {
        InputStream inputStream = ResourceUtil.getResource("res/GuardianSkills.json");
        try {
            Reader reader = new InputStreamReader(inputStream);
            Type collectionType = new TypeToken<List<GuardianBtItemList>>() {}.getType();
            Gson gson = new Gson();
            guardianSkills = gson.fromJson(reader, collectionType);
        } finally {
            inputStream.close();
        }
    }

    public GuardianBtItemList findGuardianBtItemListById(int btItemId) {
        return this.guardianSkills.stream().filter(x -> x.getBtItemId() == btItemId).findFirst().orElse(null);
    }
}
