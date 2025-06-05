package org.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectUtils {

    public static <T> T deepCopy(T data) {
        final Class<T> cls = org.apache.commons.lang3.ObjectUtils.getClass(data);
        return new ObjectMapper().convertValue(data, cls);
    }
}
