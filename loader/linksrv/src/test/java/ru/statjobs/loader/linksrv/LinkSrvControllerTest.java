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

    LinkSrvController controller = new LinkSrvController(mapMock, queueMock, new JsonUtils());

    @Test
    public void createGetTest() {
        controller.createDownloadableLink(new DownloadableLink("U1", 1, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U3", 3, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U1", 1, UrlTypes.HH_RESUME, null));

        Assert.assertEquals(mapMock.get("1:U1"), "CREATE");
        Assert.assertEquals(mapMock.get("2:U2"), "CREATE");
        Assert.assertEquals(mapMock.get("3:U3"), "CREATE");
        Assert.assertEquals(3, mapMock.map.keySet().size());

        List<String> list = queueMock.map.get(LinkSrvController.QUEUE_NAME);

        Assert.assertEquals("{\"url\":\"U1\",\"sequenceNum\":1,\"handlerName\":\"HH_RESUME\",\"props\":null}", list.get(0));
        Assert.assertEquals("{\"url\":\"U2\",\"sequenceNum\":2,\"handlerName\":\"HH_RESUME\",\"props\":null}", list.get(1));
        Assert.assertEquals("{\"url\":\"U3\",\"sequenceNum\":3,\"handlerName\":\"HH_RESUME\",\"props\":null}", list.get(2));
        Assert.assertEquals(3, list.size());

        Assert.assertEquals("U3", controller.getDownloadableLink().getUrl());
        Assert.assertEquals("U2", controller.getDownloadableLink().getUrl());
        Assert.assertEquals("U1", controller.getDownloadableLink().getUrl());
        Assert.assertNull(controller.getDownloadableLink());
    }

    @Test
    public void deleteTest() {
        controller.createDownloadableLink(new DownloadableLink("U1", 1, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U3", 3, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U1", 1, UrlTypes.HH_RESUME, null));
        Assert.assertEquals(mapMock.get("1:U1"), "CREATE");
        Assert.assertEquals(mapMock.get("2:U2"), "CREATE");
        Assert.assertEquals(mapMock.get("3:U3"), "CREATE");

        controller.deleteDownloadableLink(new DownloadableLink("U1", 1, UrlTypes.HH_LIST_RESUME, null));
        controller.deleteDownloadableLink(new DownloadableLink("U2", 1, UrlTypes.HH_LIST_RESUME, null));
        controller.deleteDownloadableLink(new DownloadableLink("U1", 2, UrlTypes.HH_LIST_RESUME, null));
        Assert.assertEquals(mapMock.get("1:U1"), "DELETE");
        Assert.assertEquals(mapMock.get("2:U2"), "CREATE");
        Assert.assertEquals(mapMock.get("3:U3"), "CREATE");

        controller.deleteDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_LIST_RESUME, null));
        Assert.assertEquals(mapMock.get("1:U1"), "DELETE");
        Assert.assertEquals(mapMock.get("2:U2"), "DELETE");
        Assert.assertEquals(mapMock.get("3:U3"), "CREATE");
    }

    @Test
    public void complexTest() {
        controller.createDownloadableLink(new DownloadableLink("U1", 1, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U3", 3, UrlTypes.HH_RESUME, null));

        controller.deleteDownloadableLink(new DownloadableLink("U1", 1, UrlTypes.HH_LIST_RESUME, null));
        controller.deleteDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_LIST_RESUME, null));

        controller.createDownloadableLink(new DownloadableLink("U1", 1, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U2", 2, UrlTypes.HH_RESUME, null));
        controller.createDownloadableLink(new DownloadableLink("U3", 3, UrlTypes.HH_RESUME, null));

        Assert.assertEquals(mapMock.get("1:U1"), "DELETE");
        Assert.assertEquals(mapMock.get("2:U2"), "DELETE");
        Assert.assertEquals(mapMock.get("3:U3"), "CREATE");

        Assert.assertEquals("U3", controller.getDownloadableLink().getUrl());
        Assert.assertEquals("U2", controller.getDownloadableLink().getUrl());
        Assert.assertEquals("U1", controller.getDownloadableLink().getUrl());
    }

    @Test
    public void createHashTest() {
        Assert.assertEquals("12:url11", LinkSrvController.createHash(new DownloadableLink("url11", 12, UrlTypes.HH_LIST_RESUME, null)));
        Assert.assertEquals("0:777", LinkSrvController.createHash(new DownloadableLink("777", 0, UrlTypes.HH_LIST_RESUME, null)));
    }

}
