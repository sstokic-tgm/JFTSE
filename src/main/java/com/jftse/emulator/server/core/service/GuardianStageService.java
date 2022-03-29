package com.jftse.emulator.server.core.service;

import com.jftse.emulator.common.utilities.ResourceUtil;
import com.jftse.emulator.server.database.model.battle.GuardianStage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
public class GuardianStageService {
    private List<GuardianStage> guardianStages;

    @PostConstruct
    public void init() throws IOException {
        InputStream inputStream = ResourceUtil.getResource("res/GuardianStages.json");
        try {
            Reader reader = new InputStreamReader(inputStream);
            Type collectionType = new TypeToken<List<GuardianStage>>() {}.getType();
            Gson gson = new Gson();
            guardianStages = gson.fromJson(reader, collectionType);
        } finally {
            inputStream.close();
        }
    }
}
