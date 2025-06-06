package org.api.action;

import lombok.Getter;
import org.api.call.AuthorsApi;
import org.api.model.Author;

import java.util.List;

public class AuthorsApiActions {

    @Getter
    private final AuthorsApi authorsApi = new AuthorsApi();

    public List<Author> getAuthors() {
        return authorsApi.getAuthors().get();
    }

    public Author createAuthors(Author author) {
        return authorsApi.createAuthors(author).get();
    }

    public Author getAuthorByBookId(long bookId) {
        return authorsApi.getAuthorByBookId(bookId).get();
    }

    public Author getAuthorById(long authorId) {
        return authorsApi.getAuthorById(authorId).get();
    }

    public Author updateAuthor(long authorId, Author author) {
        return authorsApi.updateAuthor(authorId, author).get();
    }


    public void deleteAuthor(long authorId) {
        authorsApi.deleteAuthor(authorId).get();
    }
}
