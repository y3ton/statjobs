package ru.statjobs.loader.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtilsTest {

    private JsonUtils jsonUtils = new JsonUtils();

    @Test
    public void readStringTest() {
        List<Integer> li = jsonUtils.readString("[1,2,3,4,5,6]");
        Assert.assertEquals(6, li.size());
        Assert.assertEquals((Integer)1, li.get(0));
        Assert.assertEquals((Integer)6, li.get(5));
        List<String> ls = jsonUtils.readString("[\"asd\", \"asd1\"]");
        Assert.assertEquals(2, ls.size());
        Assert.assertEquals("asd", ls.get(0));
        Assert.assertEquals("asd1", ls.get(1));
        Map<String, List<Map<String, Integer>>> r = jsonUtils.readString(
                "{\"a\":[{\"b\":1,\"c\":2},{\"b\":3,\"c\":4}]," +
                        "\"d\":[{\"e\":1,\"f\":2},{\"e\":3,\"f\":4}]}");
        Assert.assertEquals((Integer) 1, r.get("a").get(0).get("b"));
        Assert.assertEquals((Integer) 4, r.get("a").get(1).get("c"));
        Assert.assertEquals((Integer) 1, r.get("d").get(0).get("e"));
    }

    @Test
    public void readEmptyStringTest() {
        Map map = jsonUtils.readString(null);
        Assert.assertTrue(map.isEmpty());
        map = jsonUtils.readString("");
        Assert.assertTrue(map.isEmpty());
        map = jsonUtils.readString(" ");
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void createStringTest() {
        Assert.assertEquals(null, jsonUtils.createString(null));
        Map<String, String> map = new HashMap<>();
        Assert.assertEquals("{}", jsonUtils.createString(map));
        map.put("a", "b");
        Assert.assertEquals("{\"a\":\"b\"}", jsonUtils.createString(map));
    }

    @Test(expected = RuntimeException.class)
    public void failParseTest() {
        jsonUtils.readString("\\/");
    }

    @Test(expected = RuntimeException.class)
    public void failSaveTest() {
        Map map = new HashMap();
        map.put(new Object(), new Object());
        jsonUtils.createString(map);
    }

}
