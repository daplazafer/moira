package com.dpf.moira.properties;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesLoader {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);
    private final Properties properties = new Properties();

    public PropertiesLoader(String propertiesFileName) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFileName)) {
            if (input == null) {
                logger.warn("Properties file '{}' not found in the classpath. Using default properties.", propertiesFileName);
            } else {
                properties.load(input);
                logger.info("Properties file '{}' loaded successfully.", propertiesFileName);
            }
        } catch (IOException ex) {
            logger.error("Error loading properties file", ex);
        }
    }

    public <T> T loadProperties(Class<T> propertiesClass) {
        try {
            T configInstance = propertiesClass.getDeclaredConstructor().newInstance();
            boolean propertiesLoaded = false;
            for (Field field : propertiesClass.getDeclaredFields()) {
                field.setAccessible(true);
                String propertyValue = properties.getProperty(field.getName());
                if (propertyValue != null) {
                    field.set(configInstance, propertyValue);
                    propertiesLoaded = true;
                }
            }
            if (propertiesLoaded) {
                logger.info("Configuration for class '{}' loaded successfully from properties.", propertiesClass.getName());
            } else {
                logger.info("No properties found to load for class '{}'. Using default values.", propertiesClass.getName());
            }
            return configInstance;
        } catch (Exception e) {
            logger.error("Error loading configuration", e);
            try {
                return propertiesClass.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new RuntimeException("Unable to create default instance of " + propertiesClass.getName(), ex);
            }
        }
    }
}

