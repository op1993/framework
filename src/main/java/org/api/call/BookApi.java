package org.api.call;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.api.BaseApiConfig;
import org.api.ResponseWrapper;
import org.api.model.Book;

import java.util.List;

public class BookApi extends BaseApiConfig {

    private static final String PATH = "/api/v1/Books";

    public ResponseWrapper<List<Book>> getBooks() {
        return ResponseWrapper.of(getClient().get(PATH), new TypeRef<>() {
        });
    }

    public ResponseWrapper<Book> createBook(Book book) {
        return ResponseWrapper.of(getClient()
                .contentType(ContentType.JSON)
                .body(book)
                .post(PATH), Book.class);
    }

    public ResponseWrapper<Book> getBook(int bookId) {
        return ResponseWrapper.of(getClient()
                .contentType(ContentType.JSON)
                .pathParam("id", bookId)
                .get(PATH + "/{id}"), Book.class);
    }

    public ResponseWrapper<Book> updateBook(int bookId, Book book) {
        return ResponseWrapper.of(getClient()
                .contentType(ContentType.JSON)
                .pathParam("id", bookId)
                .body(book)
                .put(PATH + "/{id}"), Book.class);
    }


    public ResponseWrapper<Void> deleteBook(int bookId) {
        return ResponseWrapper.of(getClient()
                .contentType(ContentType.JSON)
                .pathParam("id", bookId)
                .delete(PATH + "/{id}"), Void.class);
    }
}
