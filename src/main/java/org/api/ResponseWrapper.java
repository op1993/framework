package org.api;

import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;

import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ResponseWrapper<T> {

    private final Response response;
    private Class<T> tClass;
    private TypeRef<T> tTypeRef;
    private Path contractSchemaPath;

    public ResponseWrapper(Response response, Class<T> tClass, Path contractSchemaPath) {
        this.response = response;
        this.tClass = tClass;
        this.contractSchemaPath = contractSchemaPath;
    }

    public ResponseWrapper(Response response, TypeRef<T> tTypeRef, Path contractSchemaPath) {
        this.response = response;
        this.tTypeRef = tTypeRef;
        this.contractSchemaPath = contractSchemaPath;
    }

    public static <T> ResponseWrapper<T> of(Response response, Class<T> tClass) {
        return new ResponseWrapper<>(response, tClass, null);
    }

    public static <T> ResponseWrapper<T> of(Response response, TypeRef<T> tClass) {
        return new ResponseWrapper<>(response, tClass, null);
    }

    public static <T> ResponseWrapper<T> of(Response response, Class<T> tClass, Path contractSchemaPath) {
        return new ResponseWrapper<>(response, tClass, contractSchemaPath);
    }

    public static <T> ResponseWrapper<T> of(Response response, TypeRef<T> tClass, Path contractSchemaPath) {
        return new ResponseWrapper<>(response, tClass, contractSchemaPath);
    }

    public T get() {
        var matcher = Matchers.describedAs("Expected status code (200–299)",
                Matchers.is(Matchers.in(IntStream.rangeClosed(200, 299)
                        .boxed()
                        .collect(Collectors.toList()))));
        response.then()
                .statusCode(matcher);
        if (tClass != null) {
            return response.as(tClass);
        } else {
            return response.as(tTypeRef);
        }
    }

    public Response asRaw() {
        return response;
    }

    public ResponseValidation then() {
        return new ResponseValidation(this.response, this.contractSchemaPath);
    }



}
