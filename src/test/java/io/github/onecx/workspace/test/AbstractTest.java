package io.github.onecx.workspace.test;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static io.restassured.RestAssured.config;
import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.restassured.config.RestAssuredConfig;

@SuppressWarnings("java:S2187")
public class AbstractTest {

    static {
        config = RestAssuredConfig.config().objectMapperConfig(
                objectMapperConfig().jackson2ObjectMapperFactory(
                        (cls, charset) -> {
                            var objectMapper = new ObjectMapper();
                            objectMapper.registerModule(new JavaTimeModule());
                            objectMapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
                            return objectMapper;
                        }));
    }
}
