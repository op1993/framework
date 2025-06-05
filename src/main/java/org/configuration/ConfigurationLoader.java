package org.configuration;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.configuration.model.AutomationConfig;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.stream.Stream;

@Log4j2
public class ConfigurationLoader {

    @Getter
    private static final AutomationConfig automationConfiguration;

    static {
        automationConfiguration = loadConfig();
    }

    private static AutomationConfig loadConfig() {
        log.info("Loading configuration started");
        ObjectMapper objectMapperYaml = JsonMapper.builder(
                        new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();
        String configurationFileClassPath = "configuration/automation-application.yml";
        try (InputStream configurationFile = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(configurationFileClassPath)) {
            if (configurationFile == null) {
                throw new RuntimeException("Configuration file not found: %s".formatted(configurationFileClassPath));
            }
            AutomationConfig automationConfiguration = objectMapperYaml.readValue(configurationFile, AutomationConfig.class);
            overrideWithSystemProperties(automationConfiguration, "");
            log.info("Loading configuration finished");
            return automationConfiguration;
        } catch (IOException e) {
            log.error("Unable to load configuration", e);
            throw new RuntimeException("Unable to load configuration", e);
        }
    }

    public static void overrideWithSystemProperties(Object obj, String prefix) {
        log.debug("Updating configuration according to system properties");
        if (obj == null) return;

        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String key = prefix + field.getName();
            String value = System.getProperty(key);

            try {
                Class<?> type = field.getType();
                if (value != null) {
                    if (type == String.class) {
                        field.set(obj, value);
                    } else if (type == int.class || type == Integer.class) {
                        field.set(obj, Integer.parseInt(value));
                    } else if (type == boolean.class || type == Boolean.class) {
                        field.set(obj, Boolean.parseBoolean(value));
                    } else if (type.isEnum()) {
                        Object enumValue = Stream.of(type.getEnumConstants())
                                .filter(e -> e.toString().equalsIgnoreCase(value))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("No enum constant for: " + value));
                        field.set(obj, enumValue);
                    }
                } else if (!type.isPrimitive() && !type.isEnum() && !type.isArray()
                        && !type.getName().startsWith("java.")) {
                    Object nestedObject = field.get(obj);
                    if (nestedObject == null) {
                        nestedObject = type.getDeclaredConstructor().newInstance();
                        field.set(obj, nestedObject);
                    }
                    overrideWithSystemProperties(nestedObject, key + ".");
                }

            } catch (Exception e) {
                log.error("Unexpected error while overriding property userId={}, error={}", key, e.getMessage());
                throw new RuntimeException("Failed to override property: " + key, e);
            }
        }
    }

}
