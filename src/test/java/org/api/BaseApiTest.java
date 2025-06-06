package org.api;

import groovy.util.logging.Log4j2;
import org.api.action.BookApiActions;
import org.api.model.Book;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log4j2
public class BaseApiTest {

    protected BookApiActions bookApiActions;

    private List<Long> booksForCleanup;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        bookApiActions = new BookApiActions();
        booksForCleanup = Collections.synchronizedList(new ArrayList<>());
    }

    @AfterMethod(alwaysRun = true)
    public void cleanup() {
        booksForCleanup.forEach(id -> {
            bookApiActions.getBookApi().deleteBook(id);
        });
    }

    protected void addBookToCleanup(Book... books) {
        if (books == null) return;
        for (Book book : books) {
            if (book != null && book.getId() != null) {
                booksForCleanup.add(book.getId());
            }
        }
    }

}
