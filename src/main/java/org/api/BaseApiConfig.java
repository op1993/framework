package org.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSpecification;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.configuration.ConfigurationLoader;

import java.util.function.Function;

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
                .filter(new ThreadLoggingFilter())
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

    @Log4j2
    private static class ThreadLoggingFilter implements Filter {

        @Override
        public Response filter(FilterableRequestSpecification requestSpec,
                               FilterableResponseSpecification responseSpec,
                               FilterContext ctx) {
            boolean isMultipart = requestSpec.getContentType() != null &&
                    requestSpec.getContentType().toLowerCase().contains("multipart");
            String requestBody = isMultipart ? "[multipart content omitted]" : requestSpec.getBody();

            Response response = ctx.next(requestSpec, responseSpec);

            boolean isBinary = response.getContentType() != null &&
                    (response.getContentType().toLowerCase().contains("multipart") ||
                            response.getContentType().equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            String responseBody = isBinary ? "[binary content omitted]" : response.getBody().asString();

            Function<String, String> updateBodyTextIfEmpty = text -> StringUtils.isBlank(text) ? "-" : text;

            StringBuilder logBuilder = new StringBuilder();
            logBuilder
                    .append("Thread - ").append(Thread.currentThread().threadId()).append("\n")
                    .append("\tURL           : ").append(requestSpec.getMethod()).append(" ").append(requestSpec.getURI()).append("\n")
                    .append("\tRequest Body  : ").append(updateBodyTextIfEmpty.apply(requestBody)).append("\n")
                    .append("\tResponse Code : ").append(response.getStatusCode()).append(" ").append("\n")
                    .append("\tExec Time     : ").append(response.getTime()).append(" ms").append("\n")
                    .append("\tResponse Body : ").append(updateBodyTextIfEmpty.apply(responseBody));
            log.debug("\n{}", logBuilder);
            return response;
        }
    }

}
