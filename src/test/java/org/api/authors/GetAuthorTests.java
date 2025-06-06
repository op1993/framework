package org.api.authors;

import io.qameta.allure.*;
import org.annotations.NonRetryable;
import org.api.BaseApiTest;
import org.api.ResponseWrapper;
import org.api.model.Author;
import org.api.model.Book;
import org.api.model.ErrorModel;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Epic("Authors API")
@Feature("Authors Management")
@Story("Get Author")
public class GetAuthorTests extends BaseApiTest {

    @Test(groups = {"smoke"})
    @Severity(SeverityLevel.CRITICAL)
    public void getAuthorById() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author createdAuthor = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(createdAuthor);

        Author retrievedAuthor = authorsApiActions.getAuthorById(createdAuthor.getId());

        Assertions.assertThat(retrievedAuthor)
                .as("Author should not be null")
                .isNotNull();

        Assertions.assertThat(retrievedAuthor)
                .usingRecursiveAssertion()
                .isEqualTo(createdAuthor);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void getAuthorByNotExistingId() {
        int nonExistentId = Integer.MAX_VALUE - 1;

        var actualResponse = authorsApiActions.getAuthorsApi()
                .getAuthorById(nonExistentId).then()
                .validateStatusCode(404)
                .asObject(ErrorModel.class);

        var expectedResponse = new ErrorModel()
                .setTitle("Not Found")
                .setStatus(404);

        Assertions.assertThat(actualResponse)
                .usingRecursiveComparison()
                .ignoringFields("type", "traceId")
                .isEqualTo(expectedResponse);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void getAuthorByNegativeId() {
        int negativeId = -1;

        authorsApiActions.getAuthorsApi()
                .getAuthorById(negativeId).then()
                .validateStatusCode(404);
    }


    @Test(groups = {"smoke"})
    @Severity(SeverityLevel.CRITICAL)
    public void getAllAuthors() {
        Book book1 = bookApiActions.createBook(Book.createValidBookDTO());
        Book book2 = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book1, book2);

        Author author1 = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book1.getId()));
        Author author2 = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book2.getId()));
        addAuthorToCleanup(author1, author2);

        List<Author> allAuthors = authorsApiActions.getAuthors();

        Assertions.assertThat(allAuthors)
                .as("Authors list should not be null")
                .isNotNull();

        Assertions.assertThat(allAuthors)
                .as("Authors list should not be empty")
                .isNotEmpty();

        List<Author> authorsMatchedToAuthor1 = allAuthors.stream()
                .filter(s -> s.getId().equals(author1.getId()))
                .toList();

        Assertions.assertThat(authorsMatchedToAuthor1)
                .as("Check that only 1 author matched criteria to author #1")
                .hasSize(1);

        Assertions.assertThat(authorsMatchedToAuthor1.getFirst().getId())
                .as("Assert that BE created ID for author 1")
                .isNotNull();

        Assertions.assertThat(authorsMatchedToAuthor1.getFirst().getIdBook())
                .as("Author should be associated with correct book")
                .isEqualTo(book1.getId());
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void getAuthorByBookId() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author authorToCreate = Author.createValidAuthorDTOForBook(book.getId());
        Author createdAuthor = authorsApiActions.createAuthors(authorToCreate);
        addAuthorToCleanup(createdAuthor);

        Author retrievedAuthor = authorsApiActions.getAuthorByBookId(book.getId());

        Assertions.assertThat(retrievedAuthor)
                .as("Author should not be null")
                .isNotNull();

        Assertions.assertThat(retrievedAuthor.getIdBook())
                .as("Author's book ID should match requested book ID")
                .isEqualTo(book.getId());

        Assertions.assertThat(retrievedAuthor.getId())
                .as("Retrieved author should be the same as created")
                .isEqualTo(createdAuthor.getId());
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void getAuthorByNotExistingBookId() {
        int nonExistentBookId = Integer.MAX_VALUE - 1;

        authorsApiActions.getAuthorsApi()
                .getAuthorByBookId(nonExistentBookId).then()
                .validateStatusCode(404);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void getAuthorByBookIdWithMultipleAuthors() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author author1 = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        Author author2 = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(author1, author2);

        Author retrievedAuthor = authorsApiActions.getAuthorByBookId(book.getId());

        Assertions.assertThat(retrievedAuthor)
                .as("Author should not be null")
                .isNotNull();

        Assertions.assertThat(retrievedAuthor.getIdBook())
                .as("Author's book ID should match requested book ID")
                .isEqualTo(book.getId());

        Assertions.assertThat(retrievedAuthor.getId())
                .as("Retrieved author should be one of the created authors")
                .isIn(author1.getId(), author2.getId());
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void getAuthorByDeletedBookId() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        Author author = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(author);

        bookApiActions.deleteBook(book.getId());

        authorsApiActions.getAuthorsApi()
                .getAuthorByBookId(book.getId()).then()
                .validateStatusCode(404);
    }

    @NonRetryable
    @Test(groups = {"performance"})
    @Severity(SeverityLevel.CRITICAL)
    public void getAllAuthorsPerformanceTest() {
        int authorsToCreate = 50;

        IntStream.range(0, authorsToCreate).forEach(i -> {
            Book book = bookApiActions.createBook(Book.createValidBookDTO());
            addBookToCleanup(book);
            addAuthorToCleanup(authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId())));
        });

        int activeUsers = 10;

        List<CompletableFuture<ResponseWrapper<List<Author>>>> futures = IntStream.range(0, activeUsers)
                .mapToObj(i -> CompletableFuture.supplyAsync(() ->
                        authorsApiActions.getAuthorsApi().getAuthors()))
                .toList();

        List<ResponseWrapper<List<Author>>> responses = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        int maxResponseTimeMs = 3_000;
        boolean allResponsesWithinLimit = responses.stream()
                .map(ResponseWrapper::getExecutionTime)
                .noneMatch(s -> s > maxResponseTimeMs);

        long failedResponses = responses.stream()
                .filter(s -> s.getStatusCode() < 200 || s.getStatusCode() >= 300)
                .count();

        Assertions.assertThat(allResponsesWithinLimit)
                .as("all requests should be performed less then in %s ms".formatted(maxResponseTimeMs))
                .isTrue();

        Assertions.assertThat(failedResponses)
                .as("all requests should be performed successfully")
                .isEqualTo(0);
    }

    @NonRetryable
    @Test(groups = {"performance"})
    @Severity(SeverityLevel.NORMAL)
    public void getAuthorByIdPerformanceTest() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author author = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(author);

        int concurrentRequests = 20;

        List<CompletableFuture<ResponseWrapper<Author>>> futures = IntStream.range(0, concurrentRequests)
                .mapToObj(i -> CompletableFuture.supplyAsync(() ->
                        authorsApiActions.getAuthorsApi().getAuthorById(author.getId())))
                .toList();

        List<ResponseWrapper<Author>> responses = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        int maxResponseTimeMs = 2_000;
        boolean allResponsesWithinLimit = responses.stream()
                .map(ResponseWrapper::getExecutionTime)
                .noneMatch(s -> s > maxResponseTimeMs);

        long failedResponses = responses.stream()
                .filter(s -> s.getStatusCode() < 200 || s.getStatusCode() >= 300)
                .count();

        Assertions.assertThat(allResponsesWithinLimit)
                .as("all requests should be performed less then in %s ms".formatted(maxResponseTimeMs))
                .isTrue();

        Assertions.assertThat(failedResponses)
                .as("all requests should be performed successfully")
                .isEqualTo(0);
    }
}