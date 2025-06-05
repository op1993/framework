package org.api.book;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.api.action.BookApiActions;
import org.api.model.Book;
import org.api.model.ErrorModel;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class BookTests {

    @Severity(value = SeverityLevel.MINOR)
    @Test
    public void getNotExistingBook() {
        var expectedResponse = new ErrorModel()
                .setTitle("Not Found")
                .setStatus(404);

        var actualResponse = new BookApiActions().getBookApi()
                .getBook(1000)
                .then()
                .validateStatusCode(404)
                .asObject(ErrorModel.class);

        Assertions.assertThat(actualResponse)
                .usingRecursiveComparison()
                .ignoringFields("type", "traceId")
                .isEqualTo(expectedResponse);
    }

    @Severity(value = SeverityLevel.MINOR)
    @Test
    public void deleteExistingBook() {
        BookApiActions bookApiActions = new BookApiActions();
        var book = bookApiActions.createBook(new Book()
                .setDescription("some description")
                .setTitle("BOOK"));

        bookApiActions.getBookApi()
                .deleteBook(book.getId())
                .then()
                .validateStatusCode(200);
    }

    @Test
    public void deleteBookWithNotExistingId() {
        var expectedResponse = new ErrorModel()
                .setTitle("Not Found")
                .setStatus(404);

        var actualResponse = new BookApiActions().getBookApi()
                .deleteBook(-1000)
                .then()
                .validateStatusCode(404)
                .asObject(ErrorModel.class);

        Assertions.assertThat(actualResponse)
                .usingRecursiveComparison()
                .ignoringFields("type", "traceId")
                .isEqualTo(expectedResponse);
    }


}
