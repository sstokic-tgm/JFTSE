package com.jftse.emulator.server.database.converters;

import com.jftse.emulator.server.database.model.auctionhouse.PriceType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class PriceTypeConverter implements AttributeConverter<PriceType, String> {
    @Override
    public String convertToDatabaseColumn(PriceType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getName();
    }

    @Override
    public PriceType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Stream.of(PriceType.values())
                .filter(glt -> glt.getName().equals(dbData))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
