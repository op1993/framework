package org.api;

import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.hamcrest.Matchers;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ResponseWrapper<T> {

    private final Response response;
    private Class<T> tClass;
    private TypeRef<T> tTypeRef;

    public ResponseWrapper(Response response, Class<T> tClass) {
        this.response = response;
        this.tClass = tClass;
    }

    public ResponseWrapper(Response response, TypeRef<T> tTypeRef) {
        this.response = response;
        this.tTypeRef = tTypeRef;
    }

    public static <T> ResponseWrapper<T> of(Response response, Class<T> tClass) {
        return new ResponseWrapper<>(response, tClass);
    }

    public static <T> ResponseWrapper<T> of(Response response, TypeRef<T> tClass) {
        return new ResponseWrapper<>(response, tClass);
    }

    public T get() {
        var matcher = Matchers.describedAs("Expected status code (200–299)",
                Matchers.is(Matchers.in(IntStream.rangeClosed(200, 299)
                        .boxed()
                        .collect(Collectors.toList()))));
        response.then().assertThat()
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
}
