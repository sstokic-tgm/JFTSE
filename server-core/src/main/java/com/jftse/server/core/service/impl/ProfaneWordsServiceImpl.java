package com.jftse.server.core.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jftse.emulator.common.utilities.ResourceUtil;
import com.jftse.server.core.service.ProfaneWordsService;
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
public class ProfaneWordsServiceImpl implements ProfaneWordsService {
    private List<String> profaneWords;

    @Override
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

    @Override
    public boolean textContainsProfaneWord(String text) {
        return profaneWords.stream()
                .anyMatch(x -> text.toLowerCase(Locale.ROOT).contains(x));
    }
}
