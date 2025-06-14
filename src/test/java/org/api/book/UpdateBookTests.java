package org.api.book;

import io.qameta.allure.*;
import org.api.BaseApiTest;
import org.api.model.Book;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import org.utils.ObjectUtils;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Epic("Books API")
@Feature("Books Management")
@Story("Update Book")
public class UpdateBookTests extends BaseApiTest {

    @Test(groups = {"smoke"}, description = "Update already existing book")
    @Severity(SeverityLevel.CRITICAL)
    public void updateBook() {
        Book originalBook = bookApiActions.createBook(Book.createValidBookDTO()
                .setExcerpt("test"));
        addBookToCleanup(originalBook);

        Book updateBookDTO = new Book()
                .setId(null)
                .setDescription("new " + originalBook.getDescription())
                .setTitle("new " + originalBook.getTitle())
                .setPageCount(originalBook.getPageCount() + 2)
                .setExcerpt("new" + originalBook.getExcerpt());

        Book updatedBook = bookApiActions.updateBook(originalBook.getId(), updateBookDTO);

        updateBookDTO.setId(originalBook.getId());
        updateBookDTO.setPublishDate(originalBook.getPublishDate());
        Assertions.assertThat(updatedBook)
                .usingRecursiveAssertion()
                .isEqualTo(updateBookDTO);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateBookWithInvalidId() {
        int nonExistentId = Integer.MAX_VALUE - 1;
        bookApiActions.getBookApi()
                .updateBook(nonExistentId, Book.createValidBookDTO())
                .then()
                .validateStatusCode(404);
    }

    @Test(groups = {"regression", "security"})
    @Severity(SeverityLevel.CRITICAL)
    public void updateBookWithSqlInjection() {
        Book originalBook = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(originalBook);
        String sqlInjection = "'; DROP TABLE book; --";
        Book updateBookDTO = ObjectUtils.deepCopy(originalBook)
                .setId(null)
                .setDescription(sqlInjection);

        bookApiActions.updateBook(originalBook.getId(), updateBookDTO);

        List<Book> books = bookApiActions.getBooks();
        Assertions.assertThat(books)
                .as("Table 'book' should not be deleted and should contain records")
                .isNotEmpty();

    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateBookWithSpecialChar() {
        Book originalBook = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(originalBook);
        Book updateBookDTO = ObjectUtils.deepCopy(originalBook)
                .setId(null)
                .setDescription(originalBook.getDescription() + "#@");

        var updatedBook = bookApiActions.updateBook(originalBook.getId(), updateBookDTO);
        updateBookDTO.setId(originalBook.getId());
        Assertions.assertThat(updatedBook)
                .usingRecursiveAssertion()
                .isEqualTo(updateBookDTO);

    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.CRITICAL)
    public void updateBookWithNullValues() {
        Book originalBook = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(originalBook);

        var updatedBook = bookApiActions.getBookApi()
                .updateBook(originalBook.getId(), new Book())
                .then()
                .validateStatusCode(400);
        // todo check error object
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateBookWithMaxCharDescription() {
        Book originalBook = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(originalBook);

        Book updateBookDTO = ObjectUtils.deepCopy(originalBook)
                .setId(null)
                .setDescription("A".repeat(10000));

        // todo discuss what is the limit
        var updatedBook = bookApiActions.updateBook(originalBook.getId(), updateBookDTO);
        updateBookDTO.setId(originalBook.getId());
        Assertions.assertThat(updatedBook)
                .usingRecursiveAssertion()
                .isEqualTo(updateBookDTO);

    }


    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateBookWithMoreAllowedCharsDescription() {
        Book originalBook = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(originalBook);
        // todo discuss what is the limit
        bookApiActions.getBookApi()
                .updateBook(originalBook.getId(), ObjectUtils.deepCopy(originalBook)
                        .setDescription("A".repeat(100001)))
                .then()
                .validateStatusCode(400);
        // todo check error object
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateBookWithEmptyDescription() {
        Book originalBook = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(originalBook);
        var updateBookDTO = ObjectUtils.deepCopy(originalBook)
                .setDescription("");
        var updatedBook = bookApiActions
                .updateBook(originalBook.getId(), updateBookDTO);
        updateBookDTO.setId(originalBook.getId());
        Assertions.assertThat(updatedBook)
                .usingRecursiveAssertion()
                .isEqualTo(updateBookDTO);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateBookWithNewPublishDate() {
        // discuss if publish date can be updated

        Book originalBook = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(originalBook);
        String publishDate = String.format("\"publishDate\": \"%s\"", Instant.now().toString());

        var updateBookDTO = ObjectUtils.deepCopy(originalBook)
                .setPublishDate(publishDate);

        bookApiActions
                .getBookApi()
                .updateBook(originalBook.getId(), updateBookDTO)
                .then()
                .validateStatusCode(400);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateBookWithAllEmptyFields() {
        Book originalBook = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(originalBook);

        bookApiActions
                .getBookApi()
                .updateBook(originalBook.getId(), new Book())
                .then()
                .validateStatusCode(400);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateBookWithMaxCharTitle() {
        Book originalBook = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(originalBook);

        // todo discuss what is the limit
        Book updateBookDTO = ObjectUtils.deepCopy(originalBook)
                .setId(null)
                .setTitle("A".repeat(10000));


        var updatedBook = bookApiActions.updateBook(originalBook.getId(), updateBookDTO);
        updateBookDTO.setId(originalBook.getId());
        Assertions.assertThat(updatedBook)
                .usingRecursiveAssertion()
                .isEqualTo(updateBookDTO);

    }


    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateBookWithMoreAllowedCharsTitle() {
        Book originalBook = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(originalBook);
        // todo discuss what is the limit
        bookApiActions.getBookApi()
                .updateBook(originalBook.getId(), ObjectUtils.deepCopy(originalBook)
                        .setTitle("A".repeat(100001)))
                .then()
                .validateStatusCode(400);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {"regression"})
    public void updateBookIsIdempotent() {
        Book originalBook = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(originalBook);

        Book updateData = Book.createValidBookDTO();

        Book firstUpdate = bookApiActions.updateBook(originalBook.getId(), updateData);

        Book secondUpdate = bookApiActions.updateBook(originalBook.getId(), updateData);

        Assertions.assertThat(firstUpdate)
                .usingRecursiveComparison()
                .isEqualTo(secondUpdate);
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {"regression"})
    public void concurrentUpdateOfSameBook() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Book update1 = Book.createValidBookDTO().setTitle("Update1");
        Book update2 = Book.createValidBookDTO().setTitle("Update2");

        CompletableFuture<Book> future1 = CompletableFuture.supplyAsync(() ->
                bookApiActions.updateBook(book.getId(), update1)
        );
        CompletableFuture<Book> future2 = CompletableFuture.supplyAsync(() ->
                bookApiActions.updateBook(book.getId(), update2)
        );

        Book result1 = future1.join();
        Book result2 = future2.join();
        Book finalBook = bookApiActions.getBook(book.getId());
        Assertions.assertThat(finalBook.getTitle())
                .isIn(result1.getTitle(), result2.getTitle());
    }

}
