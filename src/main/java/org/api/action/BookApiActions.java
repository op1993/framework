package org.api.action;

import io.qameta.allure.Step;
import lombok.Getter;
import org.api.call.BookApi;
import org.api.model.Book;

import java.util.List;


public class BookApiActions {

    @Getter
    private final BookApi bookApi = new BookApi();

    @Step("Get all books")
    public List<Book> getBooks() {
        return bookApi.getBooks().get();
    }

    @Step("Create book with title: `{book.title}`")
    public Book createBook(Book book) {
        return bookApi.createBook(book).get();
    }

    @Step("Get book by id: {id}")
    public Book getBook(long id) {
        return bookApi.getBook(id).get();
    }

    public Book getBook(Book book) {
        return bookApi.getBook(book.getId()).get();
    }

    @Step("Update book by id: {id}")
    public Book updateBook(long bookId, Book book) {
        return bookApi.updateBook(bookId, book).get();
    }

    public void deleteBook(long bookId) {
        bookApi.deleteBook(bookId).get();
    }

}
