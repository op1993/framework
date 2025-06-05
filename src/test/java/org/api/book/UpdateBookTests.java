package org.api.book;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.api.BaseApiTest;
import org.api.model.Book;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import org.utils.ObjectUtils;

import java.time.Instant;
import java.util.List;

@Story("Update Book")
public class UpdateBookTests extends BaseApiTest {

    @Test(groups = {"smoke"})
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

    @Test(groups = {"regression"})
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
                .setDescription("A" .repeat(10000));

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
                        .setDescription("A" .repeat(100001)))
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
                .setTitle("A" .repeat(10000));


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
                        .setTitle("A" .repeat(100001)))
                .then()
                .validateStatusCode(400);
    }

}
