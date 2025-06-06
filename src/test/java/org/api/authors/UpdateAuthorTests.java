package org.api.authors;

import io.qameta.allure.*;
import org.api.BaseApiTest;
import org.api.model.Author;
import org.api.model.Book;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import org.utils.ObjectUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Epic("Authors API")
@Feature("Authors Management")
@Story("Update Author")
public class UpdateAuthorTests extends BaseApiTest {

    @Test(groups = {"smoke"}, description = "Update already existing author")
    @Severity(SeverityLevel.CRITICAL)
    public void updateAuthor() {
        Book originalBook = bookApiActions.createBook(Book.createValidBookDTO());
        Book newBook = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(originalBook, newBook);

        Author originalAuthor = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(originalBook.getId()));
        addAuthorToCleanup(originalAuthor);

        Author updateAuthorDTO = new Author()
                .setId(null)
                .setFirstName("Updated " + originalAuthor.getFirstName())
                .setLastName("Updated " + originalAuthor.getLastName())
                .setIdBook(newBook.getId());

        Author updatedAuthor = authorsApiActions.updateAuthor(originalAuthor.getId(), updateAuthorDTO);

        updateAuthorDTO.setId(originalAuthor.getId());
        Assertions.assertThat(updatedAuthor)
                .usingRecursiveAssertion()
                .isEqualTo(updateAuthorDTO);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateAuthorWithInvalidId() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        int nonExistentId = Integer.MAX_VALUE - 1;

        authorsApiActions.getAuthorsApi()
                .updateAuthor(nonExistentId, Author.createValidAuthorDTOForBook(book.getId()))
                .then()
                .validateStatusCode(404);
    }

    @Test(groups = {"regression", "security"})
    @Severity(SeverityLevel.CRITICAL)
    public void updateAuthorWithSqlInjection() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author originalAuthor = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(originalAuthor);

        String sqlInjection = "'; DROP TABLE authors; --";
        Author updateAuthorDTO = ObjectUtils.deepCopy(originalAuthor)
                .setId(null)
                .setLastName(sqlInjection);

        authorsApiActions.updateAuthor(originalAuthor.getId(), updateAuthorDTO);

        List<Author> authors = authorsApiActions.getAuthors();
        Assertions.assertThat(authors)
                .as("Table 'authors' should not be deleted and should contain records")
                .isNotEmpty();
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateAuthorWithSpecialChar() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author originalAuthor = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(originalAuthor);

        Author updateAuthorDTO = ObjectUtils.deepCopy(originalAuthor)
                .setId(null)
                .setFirstName(originalAuthor.getFirstName() + "#@")
                .setLastName(originalAuthor.getLastName() + "$%");

        var updatedAuthor = authorsApiActions.updateAuthor(originalAuthor.getId(), updateAuthorDTO);
        updateAuthorDTO.setId(originalAuthor.getId());

        Assertions.assertThat(updatedAuthor)
                .usingRecursiveAssertion()
                .isEqualTo(updateAuthorDTO);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateAuthorWithUnicodeChars() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author originalAuthor = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(originalAuthor);

        Author updateAuthorDTO = ObjectUtils.deepCopy(originalAuthor)
                .setId(null)
                .setFirstName("Олександр")
                .setLastName("Українець");

        var updatedAuthor = authorsApiActions.updateAuthor(originalAuthor.getId(), updateAuthorDTO);
        updateAuthorDTO.setId(originalAuthor.getId());

        Assertions.assertThat(updatedAuthor)
                .usingRecursiveAssertion()
                .isEqualTo(updateAuthorDTO);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.CRITICAL)
    public void updateAuthorWithNullValues() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author originalAuthor = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(originalAuthor);

        authorsApiActions.getAuthorsApi()
                .updateAuthor(originalAuthor.getId(), new Author())
                .then()
                .validateStatusCode(400);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateAuthorWithMaxCharFirstName() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author originalAuthor = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(originalAuthor);

        Author updateAuthorDTO = ObjectUtils.deepCopy(originalAuthor)
                .setId(null)
                .setFirstName("A".repeat(10000));

        var updatedAuthor = authorsApiActions.updateAuthor(originalAuthor.getId(), updateAuthorDTO);
        updateAuthorDTO.setId(originalAuthor.getId());

        Assertions.assertThat(updatedAuthor)
                .usingRecursiveAssertion()
                .isEqualTo(updateAuthorDTO);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateAuthorWithMoreAllowedCharsFirstName() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author originalAuthor = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(originalAuthor);

        authorsApiActions.getAuthorsApi()
                .updateAuthor(originalAuthor.getId(), ObjectUtils.deepCopy(originalAuthor)
                        .setFirstName("A".repeat(100001)))
                .then()
                .validateStatusCode(400);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateAuthorWithEmptyFirstName() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author originalAuthor = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(originalAuthor);

        var updateAuthorDTO = ObjectUtils.deepCopy(originalAuthor)
                .setFirstName("");

        var updatedAuthor = authorsApiActions.updateAuthor(originalAuthor.getId(), updateAuthorDTO);
        updateAuthorDTO.setId(originalAuthor.getId());

        Assertions.assertThat(updatedAuthor)
                .usingRecursiveAssertion()
                .isEqualTo(updateAuthorDTO);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateAuthorWithNegativeBookId() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author originalAuthor = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(originalAuthor);

        authorsApiActions.getAuthorsApi()
                .updateAuthor(originalAuthor.getId(), ObjectUtils.deepCopy(originalAuthor)
                        .setIdBook(-1L))
                .then()
                .validateStatusCode(400);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateAuthorWithNullBookId() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author originalAuthor = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(originalAuthor);

        authorsApiActions.getAuthorsApi()
                .updateAuthor(originalAuthor.getId(), ObjectUtils.deepCopy(originalAuthor)
                        .setIdBook(null))
                .then()
                .validateStatusCode(400);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateAuthorWithNonExistentBookId() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author originalAuthor = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(originalAuthor);

        long nonExistentBookId = 999999L;

        authorsApiActions.getAuthorsApi()
                .updateAuthor(originalAuthor.getId(), ObjectUtils.deepCopy(originalAuthor)
                        .setIdBook(nonExistentBookId))
                .then()
                .validateStatusCode(400);
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateAuthorBookAssociation() {
        Book book1 = bookApiActions.createBook(Book.createValidBookDTO());
        Book book2 = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book1, book2);

        Author originalAuthor = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book1.getId()));
        addAuthorToCleanup(originalAuthor);

        Author updateAuthorDTO = ObjectUtils.deepCopy(originalAuthor)
                .setId(null)
                .setIdBook(book2.getId());

        Author updatedAuthor = authorsApiActions.updateAuthor(originalAuthor.getId(), updateAuthorDTO);

        Assertions.assertThat(updatedAuthor.getIdBook())
                .as("Author should be associated with the new book")
                .isEqualTo(book2.getId());

        Assertions.assertThat(updatedAuthor.getId())
                .as("Author ID should remain the same")
                .isEqualTo(originalAuthor.getId());
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {"regression"})
    public void updateAuthorIsIdempotent() {
        Book book = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book);

        Author originalAuthor = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book.getId()));
        addAuthorToCleanup(originalAuthor);

        Author updateData = Author.createValidAuthorDTOForBook(book.getId());

        Author firstUpdate = authorsApiActions.updateAuthor(originalAuthor.getId(), updateData);
        Author secondUpdate = authorsApiActions.updateAuthor(originalAuthor.getId(), updateData);

        Assertions.assertThat(firstUpdate)
                .usingRecursiveComparison()
                .isEqualTo(secondUpdate);
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {"regression"})
    public void concurrentUpdateOfSameAuthor() {
        Book book1 = bookApiActions.createBook(Book.createValidBookDTO());
        Book book2 = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book1, book2);

        Author author = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book1.getId()));
        addAuthorToCleanup(author);

        Author update1 = Author.createValidAuthorDTOForBook(book1.getId()).setFirstName("Update1");
        Author update2 = Author.createValidAuthorDTOForBook(book2.getId()).setFirstName("Update2");

        CompletableFuture<Author> future1 = CompletableFuture.supplyAsync(() ->
                authorsApiActions.updateAuthor(author.getId(), update1)
        );
        CompletableFuture<Author> future2 = CompletableFuture.supplyAsync(() ->
                authorsApiActions.updateAuthor(author.getId(), update2)
        );

        Author result1 = future1.join();
        Author result2 = future2.join();
        Author finalAuthor = authorsApiActions.getAuthorById(author.getId());

        Assertions.assertThat(finalAuthor.getFirstName())
                .isIn(result1.getFirstName(), result2.getFirstName());

        Assertions.assertThat(finalAuthor.getIdBook())
                .isIn(book1.getId(), book2.getId());
    }

    @Test(groups = {"regression"})
    @Severity(SeverityLevel.NORMAL)
    public void updateAuthorAfterBookDeletion() {
        Book book1 = bookApiActions.createBook(Book.createValidBookDTO());
        Book book2 = bookApiActions.createBook(Book.createValidBookDTO());
        addBookToCleanup(book2);

        Author author = authorsApiActions.createAuthors(Author.createValidAuthorDTOForBook(book1.getId()));
        addAuthorToCleanup(author);

        bookApiActions.deleteBook(book1.getId());

        Author updateData = ObjectUtils.deepCopy(author)
                .setId(null)
                .setIdBook(book2.getId())
                .setFirstName("Updated after book deletion");

        Author updatedAuthor = authorsApiActions.updateAuthor(author.getId(), updateData);

        Assertions.assertThat(updatedAuthor.getIdBook())
                .as("Author should be associated with the new book")
                .isEqualTo(book2.getId());

        Assertions.assertThat(updatedAuthor.getFirstName())
                .as("Author name should be updated")
                .isEqualTo("Updated after book deletion");
    }
}