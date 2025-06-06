package org.api.book;

import io.qameta.allure.*;
import org.api.BaseApiTest;
import org.api.model.Book;
import org.api.model.ErrorModel;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

@Epic("Books API")
@Feature("Books Management")
@Story("Delete Book")
public class DeleteBookTests extends BaseApiTest {

    @Severity(value = SeverityLevel.CRITICAL)
    @Test(groups = {"smoke"})
    public void deleteExistingBook() {
        var book = bookApiActions.createBook(Book.createValidBookDTO());

        bookApiActions.getBookApi()
                .deleteBook(book.getId())
                .then()
                .validateStatusCode(200);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {"regression"})
    public void deleteBookWithNotExistingId() {
        var expectedResponse = new ErrorModel()
                .setTitle("Not Found")
                .setStatus(404);

        int nonExistentId = Integer.MAX_VALUE - 1;

        var actualResponse = bookApiActions.getBookApi()
                .deleteBook(nonExistentId)
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
    public void deleteBookIsIdempotent() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());

        bookApiActions.getBookApi()
                .deleteBook(book.getId())
                .then()
                .validateStatusCode(200);

        bookApiActions.getBookApi()
                .deleteBook(book.getId())
                .then()
                .validateStatusCode(404);
    }




}
