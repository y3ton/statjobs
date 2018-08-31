package ru.statjobs.loader.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import ru.statjobs.loader.common.url.UrlTypes;

import java.io.IOException;
import java.util.HashMap;

public class DtoSerializeTest {

    final static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void rawSerializeTest() throws IOException {
        RawData data = new RawData(new DownloadableLink(
                "url1",
                123,
                UrlTypes.HH_VACANCY,
                new HashMap<String, String>() {{
                    put("a1", "av");
                    put("b1", "bv");
                }}),
                "{\"a\":\"ccc\",\"b\":[\"1\", \"2\"]}"
        );
        RawData newData= mapper.readValue(mapper.writeValueAsString(data), RawData.class);

        Assert.assertEquals(data.getJson(), newData.getJson());

        Assert.assertEquals(data.getLink().getHandlerName(), newData.getLink().getHandlerName());
        Assert.assertEquals(data.getLink().getUrl(), newData.getLink().getUrl());
        Assert.assertEquals(data.getLink().getSequenceNum(), newData.getLink().getSequenceNum());
        Assert.assertEquals(data.getLink().getProps().get("a1"), newData.getLink().getProps().get("a1"));
        Assert.assertEquals(data.getLink().getProps().get("b1"), newData.getLink().getProps().get("b1"));

        Assert.assertEquals(UrlTypes.HH_VACANCY,newData.getLink().getHandlerName());
        Assert.assertEquals("url1", newData.getLink().getUrl());
        Assert.assertEquals((Integer)123, newData.getLink().getSequenceNum());
    }

    @Test
    public void linkSerializeTest() throws IOException {

        DownloadableLink link = new DownloadableLink("url1231231", 123123, UrlTypes.HH_LIST_RESUME,
                new HashMap<String, String>() {{
                    put("vv1", "aavv1");
                    put("vv2", "aavv2");
                    put("vv3", "aavv3");
                }});

        DownloadableLink newLink = mapper.readValue(mapper.writeValueAsString(link), DownloadableLink.class);
        Assert.assertEquals("url1231231", newLink.getUrl());
        Assert.assertEquals((Integer) 123123, newLink.getSequenceNum());
        Assert.assertEquals(UrlTypes.HH_LIST_RESUME, newLink.getHandlerName());
        Assert.assertEquals("aavv1", newLink.getProps().get("vv1"));
        Assert.assertEquals("aavv3", newLink.getProps().get("vv3"));
    }

}
