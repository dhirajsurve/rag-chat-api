package com.rag.chat.api.rag.chat.api.utils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter(autoApply = true)
public class DoubleArrayConverter implements AttributeConverter<double[], String> {

    @Override
    public String convertToDatabaseColumn(double[] attribute) {
        if (attribute == null || attribute.length == 0) {
            return "";
        }
        return Arrays.stream(attribute)
                .mapToObj(Double::toString)
                .collect(Collectors.joining(","));
    }

    @Override
    public double[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new double[0];
        }
        return Arrays.stream(dbData.split(","))
                .mapToDouble(Double::parseDouble)
                .toArray();
    }
    public static double[] convertListToArray(List<Double> list) {
        return list.stream()
                .mapToDouble(Double::doubleValue)
                .toArray();
    }
}
