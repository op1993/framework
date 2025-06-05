package org.utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {

    public static <T> T getRandomElement(List<T> elements) {
        return elements.get(ThreadLocalRandom.current().nextInt(elements.size()));
    }

}
