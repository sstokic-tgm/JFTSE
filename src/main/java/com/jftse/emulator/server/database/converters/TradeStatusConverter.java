package com.jftse.emulator.server.database.converters;

import com.jftse.emulator.server.database.model.auctionhouse.TradeStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class TradeStatusConverter implements AttributeConverter<TradeStatus, String> {
    @Override
    public String convertToDatabaseColumn(TradeStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getName();
    }

    @Override
    public TradeStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Stream.of(TradeStatus.values())
                .filter(glt -> glt.getName().equals(dbData))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
