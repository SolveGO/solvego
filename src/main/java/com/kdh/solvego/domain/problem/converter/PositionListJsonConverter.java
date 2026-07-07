package com.kdh.solvego.domain.problem.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdh.solvego.domain.common.vo.Position;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class PositionListJsonConverter implements AttributeConverter<List<Position>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final TypeReference<List<Position>> POSITION_LIST_TYPE =
            new TypeReference<>() {
            };

    @Override
    public String convertToDatabaseColumn(List<Position> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert positions to JSON", e);
        }
    }

    @Override
    public List<Position> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, POSITION_LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert JSON to positions", e);
        }
    }
}