package org.api.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class ErrorModel {

    private String type;
    private String title;
    private int status;
    private String traceId;
}
