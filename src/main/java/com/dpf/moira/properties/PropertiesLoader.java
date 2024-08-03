package com.dpf.moira.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

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
                    Object convertedValue = convertValue(propertyValue, field.getType());
                    field.set(configInstance, convertedValue);
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

    private Object convertValue(String propertyValue, Class<?> fieldType) {
        if (fieldType == boolean.class || fieldType == Boolean.class) {
            return Boolean.parseBoolean(propertyValue);
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return Integer.parseInt(propertyValue);
        } else if (fieldType == long.class || fieldType == Long.class) {
            return Long.parseLong(propertyValue);
        } else if (fieldType == double.class || fieldType == Double.class) {
            return Double.parseDouble(propertyValue);
        } else if (fieldType == float.class || fieldType == Float.class) {
            return Float.parseFloat(propertyValue);
        } else if (fieldType == short.class || fieldType == Short.class) {
            return Short.parseShort(propertyValue);
        } else if (fieldType == byte.class || fieldType == Byte.class) {
            return Byte.parseByte(propertyValue);
        } else if (fieldType == char.class || fieldType == Character.class) {
            return propertyValue.charAt(0);
        } else {
            return propertyValue;
        }
    }
}

