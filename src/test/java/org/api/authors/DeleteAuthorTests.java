package org.api.authors;

import io.qameta.allure.*;
import org.api.BaseApiTest;
import org.api.model.Author;
import org.api.model.Book;
import org.api.model.ErrorModel;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

@Epic("Authors API")
@Feature("Authors Management")
@Story("Delete Author")
public class DeleteAuthorTests extends BaseApiTest {

    @Severity(value = SeverityLevel.CRITICAL)
    @Test(groups = {"smoke"})
    public void deleteExistingAuthor() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        var author = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));

        authorsApiActions.getAuthorsApi()
                .deleteAuthor(author.getId())
                .then()
                .validateStatusCode(200);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {"regression"})
    public void deleteAuthorWithNotExistingId() {
        var expectedResponse = new ErrorModel()
                .setTitle("Not Found")
                .setStatus(404);

        int nonExistentId = Integer.MAX_VALUE - 1;

        var actualResponse = authorsApiActions.getAuthorsApi()
                .deleteAuthor(nonExistentId)
                .then()
                .validateStatusCode(404)
                .asObject(ErrorModel.class);

        Assertions.assertThat(actualResponse)
                .usingRecursiveComparison()
                .ignoringFields("type", "traceId")
                .isEqualTo(expectedResponse);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {"regression"})
    public void deleteAuthorIsIdempotent() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author author = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));

        authorsApiActions.getAuthorsApi()
                .deleteAuthor(author.getId())
                .then()
                .validateStatusCode(200);

        authorsApiActions.getAuthorsApi()
                .deleteAuthor(author.getId())
                .then()
                .validateStatusCode(404);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {"regression"})
    public void deleteAuthorWithNegativeId() {
        int negativeId = -1;

        authorsApiActions.getAuthorsApi()
                .deleteAuthor(negativeId)
                .then()
                .validateStatusCode(404);
    }


    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {"regression"})
    public void verifyAuthorIsDeletedFromGetAll() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author author = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));

        authorsApiActions.deleteAuthor(author.getId());

        var allAuthors = authorsApiActions.getAuthors();
        boolean authorExists = allAuthors.stream()
                .anyMatch(a -> a.getId().equals(author.getId()));

        Assertions.assertThat(authorExists)
                .as("Deleted author should not appear in get all authors list")
                .isFalse();
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {"regression"})
    public void verifyAuthorIsDeletedFromGetById() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author author = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));

        authorsApiActions.deleteAuthor(author.getId());

        authorsApiActions.getAuthorsApi()
                .getAuthorById(author.getId())
                .then()
                .validateStatusCode(404);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {"regression"})
    public void verifyAuthorIsDeletedFromGetByBookId() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author author = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));

        authorsApiActions.deleteAuthor(author.getId());

        authorsApiActions.getAuthorsApi()
                .getAuthorByBookId(book.getId())
                .then()
                .validateStatusCode(404);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {"regression"})
    public void deleteAuthorButBookRemains() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author author = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));

        authorsApiActions.deleteAuthor(author.getId());

        Book retrievedBook = bookApiActions.getBook(book.getId());
        Assertions.assertThat(retrievedBook)
                .as("Book should still exist after author deletion")
                .isNotNull();

        Assertions.assertThat(retrievedBook.getId())
                .as("Book ID should match original")
                .isEqualTo(book.getId());
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {"regression"})
    public void deleteOneAuthorFromMultipleAuthorsOfSameBook() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author author1 = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        Author author2 = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(author2);

        authorsApiActions.deleteAuthor(author1.getId());

        authorsApiActions.getAuthorsApi()
                .getAuthorById(author1.getId())
                .then()
                .validateStatusCode(404);

        Author retrievedAuthor2 = authorsApiActions.getAuthorById(author2.getId());
        Assertions.assertThat(retrievedAuthor2)
                .as("Second author should still exist")
                .isNotNull();

        Assertions.assertThat(retrievedAuthor2.getId())
                .as("Second author ID should match")
                .isEqualTo(author2.getId());

        Author authorByBookId = authorsApiActions.getAuthorByBookId(book.getId());
        Assertions.assertThat(authorByBookId.getId())
                .as("Remaining author should be retrievable by book ID")
                .isEqualTo(author2.getId());
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {"regression"})
    public void deleteAuthorAfterBookDeletion() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        Author author = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));

        bookApiActions.deleteBook(book.getId());

        authorsApiActions.getAuthorsApi()
                .deleteAuthor(author.getId())
                .then()
                .validateStatusCode(200);

        authorsApiActions.getAuthorsApi()
                .getAuthorById(author.getId())
                .then()
                .validateStatusCode(404);
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {"regression"})
    public void deleteAuthorTwiceRapidly() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author author = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));

        var response1 = authorsApiActions.getAuthorsApi().deleteAuthor(author.getId());
        var response2 = authorsApiActions.getAuthorsApi().deleteAuthor(author.getId());

        response1.then().validateStatusCode(200);

        response2.then().validateStatusCode(404);
    }
}