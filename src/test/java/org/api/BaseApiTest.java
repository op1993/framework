package org.api;

import groovy.util.logging.Log4j2;
import org.api.action.AuthorsApiActions;
import org.api.action.BookApiActions;
import org.api.model.Author;
import org.api.model.Book;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log4j2
public class BaseApiTest {

    protected BookApiActions bookApiActions;
    protected AuthorsApiActions authorsApiActions;

    private List<Long> booksForCleanup;
    private List<Long> authorsForCleanup;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        bookApiActions = new BookApiActions();
        booksForCleanup = Collections.synchronizedList(new ArrayList<>());
        authorsForCleanup = Collections.synchronizedList(new ArrayList<>());
    }

    @AfterMethod(alwaysRun = true)
    public void cleanup() {
        booksForCleanup.forEach(id -> {
            try {
                bookApiActions.getBookApi().deleteBook(id);
            } catch (Throwable ignore) {
            }
        });
        authorsForCleanup.forEach(id -> {
            try {
                authorsApiActions.getAuthorsApi().deleteAuthor(id);
            } catch (Throwable ignore) {
            }
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

    protected void addAuthorToCleanup(Author... authors) {
        if (authors == null) return;
        for (Author author : authors) {
            if (author != null && author.getId() != null) {
                authorsForCleanup.add(author.getId());
            }
        }
    }

}
