package ru.statjobs.loader.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class JsonUtilsIT {

    private JsonUtils jsonUtils = new JsonUtils();

    @Test
    public void readResourceTest() {
        String json = "{\"a\":[{\"b\":1},{\"c\":2}],\"d\":[{\"e\":3},{\"f\":4},{\"g\":5}]}";
        Map<String, List<Map<String, Integer>>> r = jsonUtils.readString (json);
        Assert.assertEquals((Integer) 1, r.get("a").get(0).get("b"));
        Assert.assertEquals((Integer) 3, r.get("d").get(0).get("e"));
        Assert.assertEquals((Integer) 5, r.get("d").get(2).get("g"));
    }
}
