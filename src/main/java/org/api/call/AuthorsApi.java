package org.api.call;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.api.BaseApiConfig;
import org.api.ResponseWrapper;
import org.api.model.Author;

import java.util.List;

public class AuthorsApi extends BaseApiConfig {

    private static final String PATH = "/api/v1//api/v1/Authors";

    public ResponseWrapper<List<Author>> getAuthors() {
        return ResponseWrapper.of(getClient().get(PATH), new TypeRef<>() {
        });
    }

    public ResponseWrapper<Author> createAuthors(Author author) {
        return ResponseWrapper.of(getClient()
                .contentType(ContentType.JSON)
                .body(author)
                .post(PATH), Author.class);
    }

    public ResponseWrapper<Author> getAuthorByBookId(long bookId) {
        return ResponseWrapper.of(getClient()
                .contentType(ContentType.JSON)
                .pathParam("idBook", bookId)
                .get(PATH + "/authors/books/{idBook}"), Author.class);
    }

    public ResponseWrapper<Author> getAuthorById(long authorId) {
        return ResponseWrapper.of(getClient()
                .contentType(ContentType.JSON)
                .pathParam("id", authorId)
                .get(PATH + "/{id}"), Author.class);
    }

    public ResponseWrapper<Author> updateAuthor(long authorId, Author author) {
        return ResponseWrapper.of(getClient()
                .contentType(ContentType.JSON)
                .pathParam("id", authorId)
                .body(author)
                .put(PATH + "/{id}"), Author.class);
    }


    public ResponseWrapper<Void> deleteAuthor(long authorId) {
        return ResponseWrapper.of(getClient()
                .contentType(ContentType.JSON)
                .pathParam("id", authorId)
                .delete(PATH + "/{id}"), Void.class);
    }
}
