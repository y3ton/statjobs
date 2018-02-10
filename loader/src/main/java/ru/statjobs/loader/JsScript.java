package ru.statjobs.loader;

import org.apache.commons.lang3.StringUtils;
import ru.statjobs.loader.utils.FileUtils;

import java.util.HashMap;
import java.util.Map;

public class JsScript {

    private final Map<String, String> scriptCache = new HashMap<>();
    private final FileUtils fileUtils;

    public JsScript(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    public String getResume() {
        return loadScript("resumeparser.js") + "\nreturn parseResume()";
    }

    public String getResumeList() {
        return loadScript("reumelistparser.js") + "\nreturn findResumes()";
    }

    private String loadScript(String scriptName) {
        String script = scriptCache.get(scriptName);
        if (StringUtils.isBlank(script)) {
            script = fileUtils.readResourceFile("parsers/" + scriptName);
            if (StringUtils.isBlank(script)) {
                throw new RuntimeException("script " + scriptName + " is blank");
            }
            scriptCache.put(scriptName, script);
        }
        return script;
    }


}
