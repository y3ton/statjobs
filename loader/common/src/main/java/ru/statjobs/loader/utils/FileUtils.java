package ru.statjobs.loader.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {

    public String getResourceFile(String name) {
        ClassLoader classLoader = getClass().getClassLoader();
        return (new File(classLoader.getResource(name).getFile())).getAbsolutePath();
    }

    public String readResourceFile(String name) {
        return readFile(getResourceFile(name));
    }

    public String readFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
