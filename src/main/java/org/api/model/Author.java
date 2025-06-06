package org.api.model;

import lombok.Data;

@Data
public class Author {

    private Long id;
    private Long idBook;
    private String firstName;
    private String lastName;
}
