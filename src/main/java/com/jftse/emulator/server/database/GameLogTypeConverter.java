package com.jftse.emulator.server.database;

import com.jftse.emulator.server.database.model.tutorial.GameLogType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class GameLogTypeConverter implements AttributeConverter<GameLogType, String> {
    @Override
    public String convertToDatabaseColumn(GameLogType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getName();
    }

    @Override
    public GameLogType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Stream.of(GameLogType.values())
                .filter(glt -> glt.getName().equals(dbData))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
