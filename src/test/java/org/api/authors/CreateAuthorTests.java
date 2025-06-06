package org.api.authors;

import io.qameta.allure.*;
import org.annotations.NonRetryable;
import org.api.BaseApiTest;
import org.api.model.Author;
import org.api.model.Book;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Epic("Authors API")
@Feature("Authors Management")
@Story("Create Author")
public class CreateAuthorTests extends BaseApiTest {

    @Test(groups = {"smoke"})
    @Severity(SeverityLevel.CRITICAL)
    public void createNewAuthor() {
        Book book1 = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book1);

        Author authorToCreate1 = Author.createValidAuthorDTOForBook(book1.getId());
        Author authorToCreate2 = Author.createValidAuthorDTO();

        Author author1 = authorsApiActions.createAuthors(authorToCreate1);
        Author author2 = authorsApiActions.createAuthors(authorToCreate2);
        addAuthorToCleanup(author1, author2);

        Assertions.assertThat(author1.getId())
                .as("Created author should have an ID")
                .isNotNull()
                .isPositive();

        Assertions.assertThat(author1.getFirstName())
                .as("Created author first name should match input")
                .isEqualTo(authorToCreate1.getFirstName());

        Assertions.assertThat(author1.getLastName())
                .as("Created author last name should match input")
                .isEqualTo(authorToCreate1.getLastName());

        Assertions.assertThat(author1.getIdBook())
                .as("Created author book ID should match input")
                .isEqualTo(book1.getId());

        Assertions.assertThat(author2.getIdBook())
                .as("Created author book ID should match input")
                .isEqualTo(null);

        Assertions.assertThat(author1.getId())
                .as("Next author should have another ID")
                .isNotEqualTo(author2.getId());
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void createAuthorWithSpecialChars() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author authorToCreate = Author.createValidAuthorDTOForBook(book.getId());
        authorToCreate
                .setFirstName(authorToCreate.getFirstName() + "@#$%^&*()")
                .setLastName(authorToCreate.getLastName() + "@#$%^&*()");

        Author author = authorsApiActions.createAuthors(authorToCreate);
        addAuthorToCleanup(author);

        Assertions.assertThat(author.getFirstName())
                .as("Created author first name should match input")
                .isEqualTo(authorToCreate.getFirstName());

        Assertions.assertThat(author.getLastName())
                .as("Created author last name should match input")
                .isEqualTo(authorToCreate.getLastName());
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void createAuthorWithUnicodeChars() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author authorToCreate = Author.createValidAuthorDTOForBook(book.getId())
                .setFirstName("Олександр")
                .setLastName("Українець");

        Author author = authorsApiActions.createAuthors(authorToCreate);
        addAuthorToCleanup(author);

        Assertions.assertThat(author.getFirstName())
                .as("Created author first name should match input")
                .isEqualTo(authorToCreate.getFirstName());

        Assertions.assertThat(author.getLastName())
                .as("Created author last name should match input")
                .isEqualTo(authorToCreate.getLastName());
    }

    @Test(groups = {"regression", "security"})
    @Severity(SeverityLevel.CRITICAL)
    public void createAuthorWithSQLInjection() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author authorToCreate = Author.createValidAuthorDTOForBook(book.getId());
        String sqlInjection = "'; DROP TABLE authors; --";
        authorToCreate.setLastName(authorToCreate.getLastName() + sqlInjection);

        Author author = authorsApiActions.createAuthors(authorToCreate);
        addAuthorToCleanup(author);

        Assertions.assertThat(author.getLastName())
                .as("Created author last name should match input")
                .isEqualTo(authorToCreate.getLastName());

        List<Author> authors = authorsApiActions.getAuthors();
        Assertions.assertThat(authors)
                .as("Table 'authors' should not be deleted and should contain records")
                .isNotEmpty();
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void createAuthorWithoutRequiredParams() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author authorToCreate = new Author().setIdBook(book.getId());

        authorsApiActions.getAuthorsApi()
                .createAuthors(authorToCreate).then()
                .validateStatusCode(400);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void createAuthorWithLongName() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        String longFirstName = "A".repeat(10000);
        Author author = Author.createValidAuthorDTOForBook(book.getId()).setFirstName(longFirstName);

        authorsApiActions.getAuthorsApi()
                .createAuthors(author)
                .then()
                .validateStatusCode(400);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void createAuthorWithNullBookId() {
        Author author = Author.createValidAuthorDTO().setIdBook(null);

        // todo Depending on API business logic, this might return 400 or create author with book id null
        authorsApiActions.getAuthorsApi()
                .createAuthors(author)
                .then()
                .validateStatusCode(400);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void createAuthorWithNegativeBookId() {
        Author author = Author.createValidAuthorDTO().setIdBook(-1L);

        authorsApiActions.getAuthorsApi()
                .createAuthors(author)
                .then()
                .validateStatusCode(400);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void createAuthorWithNonExistentBookId() {
        long nonExistentBookId = 999999L;
        Author author = Author.createValidAuthorDTO().setIdBook(nonExistentBookId);

        authorsApiActions.getAuthorsApi()
                .createAuthors(author)
                .then()
                .validateStatusCode(400);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void createMultipleAuthorsForSameBook() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author author1 = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        Author author2 = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(author1, author2);

        Assertions.assertThat(author1.getIdBook())
                .as("First author should be associated with the book")
                .isEqualTo(book.getId());

        Assertions.assertThat(author2.getIdBook())
                .as("Second author should be associated with the same book")
                .isEqualTo(book.getId());

        Assertions.assertThat(author1.getId())
                .as("Authors should have different IDs")
                .isNotEqualTo(author2.getId());
    }

    @NonRetryable(reason = "This is performance test, we want to see the real results")
    @Test(groups = {"performance"})
    public void concurrentAuthorCreationTest() {
        int authorsToCreate = 20;

        List<Book> books = IntStream.range(0, authorsToCreate)
                .mapToObj(i -> bookApiActions.createBook(Book.createValidBookDTO()))
                .toList();
        books.forEach(this::addBookToCleanup);

        List<CompletableFuture<Author>> futures = IntStream.range(0, authorsToCreate)
                .mapToObj(i -> CompletableFuture.supplyAsync(() ->
                        authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(books.get(i).getId()))))
                .toList();

        List<Author> authors = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        authors.forEach(this::addAuthorToCleanup);

        Assertions.assertThat(authors).hasSize(authorsToCreate);
        Assertions.assertThat(authors.stream().map(Author::getId).distinct().count())
                .as("All authors should have unique IDs")
                .isEqualTo(authorsToCreate);

        authors.forEach(author -> {
            Assertions.assertThat(author.getIdBook())
                    .as("Author should have a valid book ID")
                    .isNotNull()
                    .isPositive();
        });
    }
}