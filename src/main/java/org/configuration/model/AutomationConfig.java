package org.configuration.model;

import lombok.Data;

@Data
public class AutomationConfig {

    private ApplicationConfiguration application;
    private ExecutionConfiguration execution;

}
