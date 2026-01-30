package org.tkit.onecx.workspace.domain.mappers;

import jakarta.inject.Inject;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.FormatMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.hibernate.orm.JsonFormat;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;

@JsonFormat
@PersistenceUnitExtension
public class JsonFormatMapper implements FormatMapper {
    @Inject
    ObjectMapper objectMapper;

    @Override
    public <T> T fromString(CharSequence charSequence, JavaType<T> javaType, WrapperOptions wrapperOptions) {
        if (charSequence == null || charSequence.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(charSequence.toString(),
                    objectMapper.getTypeFactory().constructType(javaType.getJavaType()));
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing JSON", e);
        }
    }

    @Override
    public <T> String toString(T t, JavaType<T> javaType, WrapperOptions wrapperOptions) {
        if (t == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(t);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing to JSON", e);
        }
    }
}
