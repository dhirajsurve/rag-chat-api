package com.rag.chat.api.rag.chat.api.utils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.stream.Collectors;

@Converter
public class FloatArrayConverter implements AttributeConverter<float[], String> {

    @Override
    public String convertToDatabaseColumn(float[] attribute) {
        if (attribute == null) {
            return null;
        }
       return Arrays.toString(attribute).split(",").toString();
    }

    @Override
    public float[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new float[0];
        }
        // Convert comma-separated string back to float array
        double[] doubleArray = Arrays.stream(dbData.split(","))
                .mapToDouble(Double::parseDouble)
                .toArray();
        return mapToFloat(doubleArray);
    }

    private static float[] mapToFloat(double[] doubles) {
        float[] floats = new float[doubles.length];
        for (int i = 0; i < doubles.length; i++) {
            floats[i] = (float) doubles[i];
        }
        return floats;
    }
}
