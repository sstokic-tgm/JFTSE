package com.jftse.emulator.server.core.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jftse.emulator.common.utilities.ResourceUtil;
import com.jftse.entities.database.model.battle.WillDamage;
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
public class WillDamageService {
    private List<WillDamage> willDamages;

    @PostConstruct
    public void init() throws IOException {
        InputStream inputStream = ResourceUtil.getResource("res/WillDamages.json");
        try {
            Reader reader = new InputStreamReader(inputStream);
            Type collectionType = new TypeToken<List<WillDamage>>() {}.getType();
            Gson gson = new Gson();
            willDamages = gson.fromJson(reader, collectionType);
        } finally {
            inputStream.close();
        }
    }
}
