package ru.statjobs.loader.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils {

    public Properties loadProperties(String propsFileName) {
        Properties properties = new Properties();
        try (InputStream input = PropertiesUtils.class.getClassLoader().getResourceAsStream(propsFileName)){
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

}
