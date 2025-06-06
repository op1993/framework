package org.api.book;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.annotations.NonRetryable;
import org.api.BaseApiTest;
import org.api.ResponseWrapper;
import org.api.model.Book;
import org.api.model.ErrorModel;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Story("Get Book")
public class GetBookTests extends BaseApiTest {

    @Test(groups = {"smoke"})
    @Severity(SeverityLevel.CRITICAL)
    public void getBookById() {
        Book createdBook = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(createdBook);

        Book retriviwedBook = bookApiActions.getBook(createdBook);

        Assertions.assertThat(retriviwedBook)
                .as("Book should not be null")
                .isNotNull();

        Assertions.assertThat(retriviwedBook)
                .usingRecursiveAssertion()
                .isEqualTo(createdBook);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void getBookByNotExistingId() {
        int nonExistentId = Integer.MAX_VALUE - 1;

        var actualResponse = bookApiActions.getBookApi()
                .getBook(nonExistentId).then()
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

    @Test(groups = {"smoke"})
    @Severity(SeverityLevel.CRITICAL)
    public void getAllBooks() {
        Book book1 = bookApiActions.createBook(Book.createValidBookDTO());
        Book book2 = bookApiActions.createBook(Book.createValidBookDTO());

        addBookToCleanup(book1, book2);

        List<Book> allBooks = bookApiActions.getBooks();

        Assertions.assertThat(allBooks)
                .as("Books list should not be null")
                .isNotNull();

        Assertions.assertThat(allBooks)
                .as("Books list should not be empty")
                .isNotEmpty();

        List<Book> booksMatchedToBook1 = allBooks.stream()
                .filter(s -> s.getId().equals(book1.getId()))
                .toList();

        Assertions.assertThat(booksMatchedToBook1)
                .as("Check that only 1 book matched criteria to book #1")
                .hasSize(1);
        Assertions.assertThat(booksMatchedToBook1.getFirst().getId()).as("Assert that BE created ID for book 1").isNotNull();
    }

    @NonRetryable
    @Test(groups = {"performance"})
    @Severity(SeverityLevel.CRITICAL)
    public void getAllBooksPerformanceTest() {
        int bookToCreate = 200;
        IntStream.range(0, bookToCreate).forEach(s -> {
            addBookToCleanup(bookApiActions.createBook(Book.createValidBookDTO()));
        });
        int activeUsers = 20;

        List<CompletableFuture<ResponseWrapper<List<Book>>>> futures = IntStream.range(0, activeUsers)
                .mapToObj(i -> CompletableFuture.supplyAsync(() ->
                        bookApiActions.getBookApi().getBooks()))
                .toList();

        List<ResponseWrapper<List<Book>>> responses = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        int maxResponseTimeMs = 3_000;
        boolean allResponsesWithinLimit = responses.stream()
                .map(ResponseWrapper::getExecutionTime)
                .noneMatch(s -> s > maxResponseTimeMs);

        long failedResponses = responses.stream()
                .filter(s -> s.getStatusCode() < 200 || s.getStatusCode() >= 300)
                .count();
        Assertions.assertThat(allResponsesWithinLimit).as("all requests should be performed less then in %s ms".formatted(maxResponseTimeMs))
                .isTrue();
        Assertions.assertThat(failedResponses).as("all requests should be performed successfully").isEqualTo(0);
    }
}
