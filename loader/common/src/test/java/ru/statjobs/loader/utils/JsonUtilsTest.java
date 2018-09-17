package ru.statjobs.loader.utils;

import org.junit.Assert;
import org.junit.Test;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.dto.RawData;
import ru.statjobs.loader.common.url.UrlTypes;

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
    public void readStringToObjectTest() {
        AT at1 = new AT();
        at1.setI(1);
        at1.setS("at1");

        AT at2 = new AT();
        at2.setI(2);
        at2.setS("at2");
        at2.setAt(at1);

        String json = jsonUtils.createString(at2);
        AT at = jsonUtils.readString(json, AT.class);

        Assert.assertEquals(2, at.getI());
        Assert.assertEquals("at2", at.getS());
        Assert.assertEquals(1, at.getAt().getI());
        Assert.assertEquals("at1", at.getAt().getS());
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

    @Test
    public void createStringFromObjectTest() {
        Map<String, String> prop = new HashMap();
        prop.put("c12", "cv12");
        prop.put("d12", "dv12");
        RawData rawData = new RawData(
                new DownloadableLink("url12", 123456, UrlTypes.HH_LIST_RESUME, prop),
                "{\"a12\":\"av12\", \"b12\":\"bv12\"}"
        );
        String str = jsonUtils.createString(rawData);
        Assert.assertTrue(str.contains("url12"));
        Assert.assertTrue(str.contains("123456"));
        Assert.assertTrue(str.contains("HH_LIST_RESUME"));
        Assert.assertTrue(str.contains("c12"));
        Assert.assertTrue(str.contains("cv12"));
        Assert.assertTrue(str.contains("d12"));
        Assert.assertTrue(str.contains("dv12"));

        Assert.assertTrue(str.contains("a12"));
        Assert.assertTrue(str.contains("av12"));
        Assert.assertTrue(str.contains("b12"));
        Assert.assertTrue(str.contains("bv12"));
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

    public static class AT {
        private String s;
        private AT at;
        private int i;

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        public AT getAt() {
            return at;
        }

        public void setAt(AT at) {
            this.at = at;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }
    }

}
