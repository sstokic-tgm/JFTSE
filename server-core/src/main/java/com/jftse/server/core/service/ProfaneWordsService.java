package com.jftse.server.core.service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

public interface ProfaneWordsService {
    @PostConstruct
    void init() throws IOException;

    boolean textContainsProfaneWord(String text);

    List<String> getProfaneWords();
}
