package org.api.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.utils.RandomUtils;

import java.util.stream.IntStream;

@Accessors(chain = true)
@Data
public class Book {

    private Long id;
    private String title;
    private String description;
    private Integer pageCount;
    private String excerpt;
    private String publishDate;

    public static Book createValidBookDTO() {
        return new Book()
                .setTitle(String.format("book-%s-%s", Thread.currentThread().threadId(), System.currentTimeMillis()))
                .setPageCount(RandomUtils.getRandomElement(IntStream.range(1, 1000).boxed().toList()))
                .setDescription("Random description");
    }

}

