package ru.statjobs.loader.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class JsonUtils {

    // Mapper are fully thread-safe
    private final static ObjectMapper mapper = new ObjectMapper();

    public String createString(Map map) {
        if (map == null) {
            return null;
        }
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String createString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T readString(String json) {
        if (StringUtils.isBlank(json)) {
            return (T) Collections.EMPTY_MAP;
        }
        try {
            return mapper.readValue(json, new TypeReference<T>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
