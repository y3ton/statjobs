package ru.statjobs.loader.testutils;

import org.h2.tools.RunScript;

import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;

public class H2Utils {

    public static void runScript(String resourceName, Connection connection)  {
        try {
            String path =
                    (new File((new H2Utils()).getClass().getClassLoader().getResource(resourceName).getFile()))
                    .getAbsolutePath();
            String query = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
            query = query
                    .replace("timestamp without time zone", "timestamp")
                    .replace("jsonb", "character varying(1024)");
            RunScript.execute(connection, new StringReader(query));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }




}
