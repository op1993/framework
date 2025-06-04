package org.api;

import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.ObjectAssert;

import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;

public class ResponseValidation {

    private Response response;
    private Path contractSchemaPath;

    public ResponseValidation(Response response, Path contractSchemaPath) {
        this.response = response;
        this.contractSchemaPath = contractSchemaPath;
    }

    public ResponseValidation validateStatusCode(int statusCode) {
        response.then().statusCode(statusCode);
        return this;
    }

    public ResponseValidation validateSchema() {
        return validateSchemaInternal(contractSchemaPath);
    }

    public ResponseValidation validateSchema(Path path) {
        return validateSchemaInternal(path);
    }

    public <V> V asObject(Class<V> obj) {
        return response.as(obj);
    }

    public <V> V asObject(TypeRef<V> obj) {
        return response.as(obj);
    }

    private ResponseValidation validateSchemaInternal(Path path) {
        if (path == null || !Files.exists(path)) {
            throw new IllegalArgumentException("Path for schema not provided or invalid");
        }
        response.then().body(matchesJsonSchema(path.toFile()));
        return this;
    }

    public static <T> ObjectAssert<T> assertThat(T actual) {
        return AssertionsForClassTypes.assertThat(actual);
    }

}
