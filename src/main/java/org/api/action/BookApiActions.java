package org.api.action;

import org.api.call.BookApi;
import org.api.model.Book;

import java.util.List;

public class BookApiActions {

    private final BookApi bookApi = new BookApi();

    public List<Book> getBooks() {
        return bookApi.getBooks().get();
    }

    public Book createBook(Book book) {
        return bookApi.createBook(book).get();
    }

    public Book getBook(int id) {
        return bookApi.getBook(id).get();
    }

    public Book updateBook(int bookId, Book book) {
        return bookApi.updateBook(bookId, book).get();
    }

    public void deleteBook(int bookId) {
        bookApi.deleteBook(bookId);
    }

}
