package org.api.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class Author {

    private Long id;
    private Long idBook;
    private String firstName;
    private String lastName;

    public static Author createValidAuthorDTO() {
        return new Author()
                .setFirstName(String.format("book-%s-%s", Thread.currentThread().threadId(), System.currentTimeMillis()))
                .setLastName(String.format("book-%s-%s", Thread.currentThread().threadId(), System.currentTimeMillis()));
    }

    public static Author createValidAuthorDTOForBook(Long bookId) {
        return createValidAuthorDTO().setIdBook(bookId);
    }
}
