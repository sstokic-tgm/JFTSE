package com.jftse.emulator.server.game.core.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jftse.emulator.common.utilities.ResourceUtil;
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
import java.util.Locale;

@Service
@Scope("singleton")
@Getter
public class ProfaneWordsService {
    private List<String> profaneWords;

    @PostConstruct
    public void init() throws IOException {
        InputStream inputStream = ResourceUtil.getResource("res/ProfaneWords.json");
        try {
            Reader reader = new InputStreamReader(inputStream);
            Type collectionType = new TypeToken<List<String>>() {}.getType();
            Gson gson = new Gson();
            profaneWords = gson.fromJson(reader, collectionType);
        } finally {
            inputStream.close();
        }
    }

    public boolean textContainsProfaneWord(String text) {
        return profaneWords.stream()
                .anyMatch(x -> text.toLowerCase(Locale.ROOT).contains(x));
    }
}
