package ru.statjobs.loader.linksrv;

import org.junit.Assert;
import org.junit.Test;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.url.UrlTypes;
import ru.statjobs.loader.linksrv.redismock.RedisMapMock;
import ru.statjobs.loader.linksrv.redismock.RedisQueueMock;
import ru.statjobs.loader.utils.JsonUtils;

import java.util.List;

public class LinkSrvControllerTest {

    RedisMapMock mapMock = new RedisMapMock();
    RedisQueueMock queueMock = new RedisQueueMock();

    JsonUtils json = new JsonUtils();

    LinkSrvController controller = new LinkSrvController(mapMock, queueMock, json);

    @Test
    public void createGetTest() {
        controller.createDownloadableLink(new DownloadableLink("U1", 1, UrlTypes.HH_VACANCY, null));
        controller.createDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U3", 3, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U1", 1, UrlTypes.HH_RESUME, null));

        Assert.assertEquals(json.createString(new DownloadableLink("U1", 1, UrlTypes.HH_VACANCY, null)), mapMock.get("P:1:U1"));
        Assert.assertEquals(json.createString(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null)), mapMock.get("P:2:U2"));
        Assert.assertEquals(json.createString(new DownloadableLink("U3", 3, UrlTypes.HH_RESUME, null)), mapMock.get("P:3:U3"));
        Assert.assertEquals("" , mapMock.get("C:1:U1"));
        Assert.assertEquals("" , mapMock.get("C:2:U2"));
        Assert.assertEquals("" , mapMock.get("C:3:U3"));
        Assert.assertEquals(6, mapMock.map.keySet().size());

        List<String> list = queueMock.map.get(LinkSrvController.QUEUE_NAME);

        Assert.assertEquals("{\"url\":\"U1\",\"sequenceNum\":1,\"handlerName\":\"HH_VACANCY\",\"props\":null}", list.get(0));
        Assert.assertEquals("{\"url\":\"U2\",\"sequenceNum\":2,\"handlerName\":\"HH_RESUME\",\"props\":null}", list.get(1));
        Assert.assertEquals("{\"url\":\"U3\",\"sequenceNum\":3,\"handlerName\":\"HH_RESUME\",\"props\":null}", list.get(2));
        Assert.assertEquals(3, list.size());

        Assert.assertEquals("U3", controller.getDownloadableLink().getUrl());
        Assert.assertEquals("U2", controller.getDownloadableLink().getUrl());
        Assert.assertEquals("U1", controller.getDownloadableLink().getUrl());
        Assert.assertEquals(null, controller.getDownloadableLink());
        Assert.assertEquals(0, list.size());

        Assert.assertNull(controller.getDownloadableLink());
    }

    @Test
    public void deleteTest() {
        controller.createDownloadableLink(new DownloadableLink("U1", 1, UrlTypes.HH_VACANCY, null));
        controller.createDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U3", 3, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U1", 1, UrlTypes.HH_RESUME, null));

        Assert.assertEquals(json.createString(new DownloadableLink("U1", 1, UrlTypes.HH_VACANCY, null)), mapMock.get("P:1:U1"));
        Assert.assertEquals(json.createString(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null)), mapMock.get("P:2:U2"));
        Assert.assertEquals(json.createString(new DownloadableLink("U3", 3, UrlTypes.HH_RESUME, null)), mapMock.get("P:3:U3"));

        controller.deleteDownloadableLink(new DownloadableLink("U1", 1, UrlTypes.HH_LIST_RESUME, null));
        controller.deleteDownloadableLink(new DownloadableLink("U2", 1, UrlTypes.HH_LIST_RESUME, null));
        controller.deleteDownloadableLink(new DownloadableLink("U1", 2, UrlTypes.HH_LIST_RESUME, null));
        Assert.assertEquals(null, mapMock.get("P:1:U1"));
        Assert.assertEquals(json.createString(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null)), mapMock.get("P:2:U2"));
        Assert.assertEquals(json.createString(new DownloadableLink("U3", 3, UrlTypes.HH_RESUME, null)), mapMock.get("P:3:U3"));

        controller.deleteDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_LIST_RESUME, null));
        Assert.assertEquals(null, mapMock.get("P:1:U1"));
        Assert.assertEquals(null, mapMock.get("P:2:U2"));
        Assert.assertEquals(json.createString(new DownloadableLink("U3", 3, UrlTypes.HH_RESUME, null)), mapMock.get("P:3:U3"));

        Assert.assertEquals("", mapMock.get("C:1:U1"));
        Assert.assertEquals("", mapMock.get("C:2:U2"));
        Assert.assertEquals("", mapMock.get("C:3:U3"));
    }

    @Test
    public void complexTest() {
        controller.createDownloadableLink(new DownloadableLink("U1", 1, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U3", 3, UrlTypes.HH_RESUME, null));

        controller.deleteDownloadableLink(new DownloadableLink("U1", 1, UrlTypes.HH_LIST_RESUME, null));
        controller.deleteDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_LIST_RESUME, null));

        controller.createDownloadableLink(new DownloadableLink("U1", 1, UrlTypes.HH_VACANCY, null));
        controller.createDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_VACANCY, null));
        controller.createDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_VACANCY, null));
        controller.createDownloadableLink(new DownloadableLink("U3", 3, UrlTypes.HH_VACANCY, null));

        Assert.assertEquals("", mapMock.get("C:1:U1"));
        Assert.assertEquals("", mapMock.get("C:2:U2"));
        Assert.assertEquals("", mapMock.get("C:3:U3"));

        Assert.assertEquals(null, mapMock.get("P:1:U1"));
        Assert.assertEquals(null, mapMock.get("P:2:U2"));
        Assert.assertEquals(json.createString(new DownloadableLink("U3", 3, UrlTypes.HH_RESUME, null)), mapMock.get("P:3:U3"));

        Assert.assertEquals("U3", controller.getDownloadableLink().getUrl());
        Assert.assertEquals("U2", controller.getDownloadableLink().getUrl());
        Assert.assertEquals("U1", controller.getDownloadableLink().getUrl());
        Assert.assertEquals(null, controller.getDownloadableLink());
    }

    @Test
    public void createHashTest() {
        Assert.assertEquals(
                "C:12:url11",
                LinkSrvController.createHash(new DownloadableLink("url11", 12, UrlTypes.HH_LIST_RESUME, null)));
        Assert.assertEquals(
                "C:0:777",
                LinkSrvController.createHash(new DownloadableLink("777", 0, UrlTypes.HH_LIST_RESUME, null)));
    }

    @Test
    public void createProceesLinkHashTest() {
        Assert.assertEquals(
                "P:12:qqqq",
                LinkSrvController.createProceesLinkHash(new DownloadableLink("qqqq", 12, UrlTypes.HH_LIST_RESUME, null)));
        Assert.assertEquals(
                "P:0:zz",
                LinkSrvController.createProceesLinkHash(new DownloadableLink("zz", 0, UrlTypes.HH_LIST_RESUME, null)));
    }

}
