package com.edsoft.order_service.mapping;

import com.edsoft.order_service.model.Item;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter(autoApply = true) // autoApply = true önemli
public class ItemsConverter implements AttributeConverter<List<Item>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Item> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize items to JSON", e);
        }
    }

    @Override
    public List<Item> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null) return new ArrayList<>();
            return objectMapper.readValue(dbData, new TypeReference<List<Item>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot deserialize items from JSON", e);
        }
    }
}

