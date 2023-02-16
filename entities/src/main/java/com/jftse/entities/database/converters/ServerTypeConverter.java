package com.jftse.entities.database.converters;

import com.jftse.entities.database.model.log.ServerType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class ServerTypeConverter implements AttributeConverter<ServerType, String> {
    @Override
    public String convertToDatabaseColumn(ServerType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getName();
    }

    @Override
    public ServerType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Stream.of(ServerType.values())
                .filter(st -> st.getName().equals(dbData))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
