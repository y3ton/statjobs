package ru.statjobs.loader.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class JsonUtilsIT {

    private JsonUtils jsonUtils = new JsonUtils();

    @Test
    public void readResourceTest() {
        Map<String, List<Map<String, Integer>>> r = jsonUtils.readResource("JsonUtilsIT.json");
        Assert.assertEquals((Integer) 1, r.get("a").get(0).get("b"));
        Assert.assertEquals((Integer) 3, r.get("d").get(0).get("e"));
        Assert.assertEquals((Integer) 5, r.get("d").get(2).get("g"));
    }
}
