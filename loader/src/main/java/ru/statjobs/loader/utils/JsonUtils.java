package ru.statjobs.loader.utils;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonUtils {

    // Mapper are fully thread-safe
    private final static ObjectMapper mapper = new ObjectMapper();

    public <T> T readString(String json) {
        try {
            return mapper.readValue(json, new TypeReference<T>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T readResource(String name) {
        String json = readFile(getResourceFile(name));
        return readString(json);
    }

    private String getResourceFile(String name) {
        ClassLoader classLoader = getClass().getClassLoader();
        return (new File(classLoader.getResource(name).getFile())).getAbsolutePath();
    }

    private String readFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




}
