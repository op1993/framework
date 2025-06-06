package org.api.book;

import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.annotations.NonRetryable;
import org.api.BaseApiTest;
import org.api.model.Book;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Story("Create Book")
public class CreateBookTest extends BaseApiTest {

    @Test(groups = {"smoke"})
    @Severity(SeverityLevel.CRITICAL)
    public void createNewBook() {
        Book bookToCreate1 = Book.createValidBookDTO();
        Book bookToCreate2 = Book.createValidBookDTO();
        Book book1 = bookApiActions.createBook(bookToCreate1);
        Book book2 = bookApiActions.createBook(bookToCreate2);
        addBookToCleanup(book1, book2);

        Assertions.assertThat(book1.getId())
                .as("Created book should have an ID")
                .isNotNull()
                .isPositive();

        Assertions.assertThat(book1.getTitle())
                .as("Created book title should match input")
                .isEqualTo(bookToCreate1.getTitle());

        Assertions.assertThat(book1.getDescription())
                .as("Created book description should match input")
                .isEqualTo(bookToCreate1.getDescription());

        Assertions.assertThat(book1.getPageCount())
                .as("Created book page count should match input")
                .isEqualTo(bookToCreate1.getPageCount());

        Assertions.assertThat(book1.getId())
                .as("Next book should have another ID")
                .isNotEqualTo(book2.getId());

    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void createNewBookWithSpecialChars() {
        Book bookToCreate1 = Book.createValidBookDTO();
        bookToCreate1
                .setTitle(bookToCreate1.getTitle() + "@#$%^&*()")
                .setDescription(bookToCreate1.getDescription() + "@#$%^&*()");

        Book book1 = bookApiActions.createBook(bookToCreate1);
        addBookToCleanup(book1);

        Assertions.assertThat(book1.getTitle())
                .as("Created book title should match input")
                .isEqualTo(bookToCreate1.getTitle());

        Assertions.assertThat(book1.getDescription())
                .as("Created book description should match input")
                .isEqualTo(bookToCreate1.getDescription());

    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void createNewBookWithUnicodeChars() {
        Book bookToCreate1 = Book.createValidBookDTO();
        bookToCreate1
                .setTitle(bookToCreate1.getTitle() + "Русскій воєнний корабль, іді...")
                .setDescription(bookToCreate1.getDescription() + "Русскій воєнний корабль, іді...");

        Book book1 = bookApiActions.createBook(bookToCreate1);
        addBookToCleanup(book1);

        Assertions.assertThat(book1.getTitle())
                .as("Created book title should match input")
                .isEqualTo(bookToCreate1.getTitle());

        Assertions.assertThat(book1.getDescription())
                .as("Created book description should match input")
                .isEqualTo(bookToCreate1.getDescription());

    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.CRITICAL)
    @Feature("Security")
    public void createNewBookWithSQLInjection() {
        Book bookToCreate1 = Book.createValidBookDTO();
        // todo get information about table name and DB. Let's assume it's oracle/postgress
        String sqlInjection = "'; DROP TABLE book; --";
        bookToCreate1
                .setDescription(bookToCreate1.getDescription() + sqlInjection);

        Book book1 = bookApiActions.createBook(bookToCreate1);
        addBookToCleanup(book1);

        // todo discuss results. Error ?
        Assertions.assertThat(book1.getDescription())
                .as("Created book description should match input")
                .isEqualTo(bookToCreate1.getDescription());

        List<Book> books = bookApiActions.getBooks();
        Assertions.assertThat(books)
                .as("Table 'book' should not be deleted and should contain records")
                .isNotEmpty();
    }

    @Ignore
    @Test(groups = {"regression"})
    @Severity(SeverityLevel.CRITICAL)
    @Feature("Security")
    public void createNewBookWithXSSScript() {
        Book bookToCreate1 = Book.createValidBookDTO();
        // todo get information about table name and DB. Let's assume it's oracle/postgress
        String xssScript = "<script>alert('XSS')</script>";
        bookToCreate1
                .setDescription(bookToCreate1.getDescription() + xssScript);

        Book book1 = bookApiActions.createBook(bookToCreate1);
        addBookToCleanup(book1);

        // todo discuss results. BE should return error?
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void createNewBookWithoutRequiredParams() {
        Book bookToCreate1 = Book.createValidBookDTO()
                .setPageCount(null)
                .setPageCount(null)
                .setPublishDate(null);

        bookApiActions.getBookApi()
                .createBook(bookToCreate1).then()
                .validateStatusCode(400);
        // todo validate error message
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void createBookWithLongDescription() {
        String longTitle = "A" .repeat(10000);
        Book book = Book.createValidBookDTO().setTitle(longTitle);

        bookApiActions.getBookApi()
                .createBook(book)
                .then()
                .validateStatusCode(400);
    }

    @Test(groups = {"regression"})
    public void createBookWithZeroPageCount() {
        Book book = Book.createValidBookDTO().setPageCount(0);

        bookApiActions.getBookApi()
                .createBook(book)
                .then()
                .validateStatusCode(400);
    }


    @NonRetryable(reason = "This is performance test, we want to see the real results")
    @Test(groups = {"performance"})
    public void concurrentBookCreationTest() {
        int bookToCreate = 20;
        List<CompletableFuture<Book>> futures = IntStream.range(0, bookToCreate)
                .mapToObj(i -> CompletableFuture.supplyAsync(() ->
                        bookApiActions.createBook(Book.createValidBookDTO())))
                .toList();

        List<Book> books = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        books.forEach(this::addBookToCleanup);

        Assertions.assertThat(books).hasSize(bookToCreate);
        Assertions.assertThat(books.stream().map(Book::getId).distinct().count())
                .as("All books should have unique IDs")
                .isEqualTo(bookToCreate);
    }
}
