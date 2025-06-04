package org.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSpecification;
import org.configuration.ConfigurationLoader;

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
                        })))
                .baseUri(ConfigurationLoader.getAutomationConfiguration().getBaseApi())
                .filter(new RestAssuredAllureFilter()
                        .setResponseAttachmentName("Response"));
    }

    private static class RestAssuredAllureFilter extends AllureRestAssured {
        @Override
        public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext filterContext) {
            String method = requestSpec.getMethod();
            String uri = requestSpec.getURI();
            String stepName = String.format("%s: %s", method, uri);
            return Allure.step(stepName, () -> super.filter(requestSpec, responseSpec, filterContext));
        }
    }

}
