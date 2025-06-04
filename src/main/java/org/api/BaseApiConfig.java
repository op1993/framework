package org.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.specification.RequestSpecification;

public abstract class BaseApiConfig {

    protected RequestSpecification getClient() {
        return RestAssured.given().config(io.restassured.config.RestAssuredConfig.config()
                .objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
                    objectMapper.setVisibility(
                            com.fasterxml.jackson.annotation.PropertyAccessor.FIELD,
                            JsonAutoDetect.Visibility.ANY
                    );
                    return objectMapper;
                }))).baseUri("https://fakerestapi.azurewebsites.net");
    }
}
