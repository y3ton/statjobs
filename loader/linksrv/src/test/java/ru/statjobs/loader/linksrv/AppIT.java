package ru.statjobs.loader.linksrv;

import ai.grakn.redismock.RedisServer;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.url.UrlTypes;
import ru.statjobs.loader.dao.DownloadableLinkDaoHttpImpl;
import ru.statjobs.loader.utils.JsonUtils;

public class AppIT {

    static Server server;
    static RedisServer redisServer;
    static HttpClient httpClient;
    static Jedis jedis;
    static DownloadableLinkDaoHttpImpl daoHttp;

    @BeforeClass
    public static void init() throws Exception {
        redisServer = RedisServer.newRedisServer();
        redisServer.start();
        jedis = new Jedis("127.0.0.1", redisServer.getBindPort());
        httpClient = new HttpClient();
        httpClient.start();
        server = App.createServer("127.0.0.1", redisServer.getBindPort(), "authkey");
        daoHttp = new DownloadableLinkDaoHttpImpl(new JsonUtils(), "http://localhost:8080/linksrv", "authkey");
        daoHttp.start();
    }

    @AfterClass
    public static void close() throws Exception {
        httpClient.stop();
        redisServer.stop();
        jedis.close();
        server.stop();
        daoHttp.stop();
    }

    @Test
    public void integrationWithDaoTest() {
        daoHttp.createDownloadableLink(new DownloadableLink("URL111", 0, UrlTypes.HH_RESUME, null));
        daoHttp.createDownloadableLink(new DownloadableLink("URL111", 0, UrlTypes.HH_RESUME, null));
        daoHttp.createDownloadableLink(new DownloadableLink("URL222", 0, UrlTypes.HH_RESUME, null));

        Assert.assertEquals(createJson("URL111"), jedis.get("P:0:URL111"));
        Assert.assertEquals("", jedis.get("C:0:URL111"));
        Assert.assertEquals(createJson("URL222"), jedis.get("P:0:URL222"));
        Assert.assertEquals("", jedis.get("C:0:URL222"));

        Assert.assertEquals("URL222", daoHttp.getDownloadableLink().getUrl());
        Assert.assertEquals("URL111", daoHttp.getDownloadableLink().getUrl());
        Assert.assertNull(daoHttp.getDownloadableLink());
        Assert.assertEquals(createJson("URL111"), jedis.get("P:0:URL111"));
        Assert.assertEquals("", jedis.get("C:0:URL111"));
        Assert.assertEquals(createJson("URL222"), jedis.get("P:0:URL222"));
        Assert.assertEquals("", jedis.get("C:0:URL222"));


        daoHttp.deleteDownloadableLink(new DownloadableLink("URL111", 0, UrlTypes.HH_RESUME, null));
        Assert.assertEquals(null, jedis.get("P:0:URL111"));
        Assert.assertEquals("", jedis.get("C:0:URL111"));
        Assert.assertEquals(createJson("URL222"), jedis.get("P:0:URL222"));
        Assert.assertEquals("", jedis.get("C:0:URL222"));
    }

    @Test
    public void integrationWithDaoFailAuthTest() {
        DownloadableLinkDaoHttpImpl daoHttpFail =
                new DownloadableLinkDaoHttpImpl(new JsonUtils(), "http://localhost:8080/linksrv", "");
        daoHttpFail.start();
        try {
            daoHttpFail.getDownloadableLink();
            Assert.fail();
        } catch (Exception ex) {
            // ok
        } finally {
            daoHttpFail.stop();
        }
    }

    @Test
    public void complexTest() throws Exception {

        ContentResponse response;

        response = httpClient.newRequest("http://127.0.0.1:8080/linksrv/get")
                .header(HttpHeader.AUTHORIZATION, "authkey")
                .method(HttpMethod.GET)
                .send();
        Assert.assertEquals("", response.getContentAsString());

        response = httpClient.POST("http://localhost:8080/linksrv/create")
            .header(HttpHeader.AUTHORIZATION, "authkey")
            .content(new StringContentProvider(createJson("u1111")), "application/json").send();
        Assert.assertEquals("true", response.getContentAsString());
        Assert.assertEquals(createJson("u1111"), jedis.get("P:0:u1111"));
        Assert.assertNotNull(jedis.get("C:0:u1111"));


        response = httpClient.POST("http://localhost:8080/linksrv/create")
                .header(HttpHeader.AUTHORIZATION, "authkey")
                .content(new StringContentProvider(createJson("u1111").replace("HH_RESUME", "HH_LIST_RESUME")), "application/json").send();
        Assert.assertEquals("true", response.getContentAsString());
        Assert.assertEquals(createJson("u1111"), jedis.get("P:0:u1111"));
        Assert.assertNotNull(jedis.get("C:0:u1111"));

        response = httpClient.POST("http://localhost:8080/linksrv/create")
                .header(HttpHeader.AUTHORIZATION, "authkey")
                .content(new StringContentProvider(createJson("u2222")), "application/json").send();
        Assert.assertEquals("true", response.getContentAsString());
        Assert.assertEquals(createJson("u2222"), jedis.get("P:0:u2222"));
        Assert.assertNotNull(jedis.get("C:0:u2222"));

        response = httpClient.newRequest("http://127.0.0.1:8080/linksrv/get")
                .header(HttpHeader.AUTHORIZATION, "authkey")
                .method(HttpMethod.GET)
                .send();
        Assert.assertTrue(response.getContentAsString().contains("u2222"));
        Assert.assertEquals(createJson("u2222"), jedis.get("P:0:u2222"));
        Assert.assertNotNull(jedis.get("C:0:u2222"));

        response = httpClient.newRequest("http://127.0.0.1:8080/linksrv/get")
                .header(HttpHeader.AUTHORIZATION, "authkey")
                .method(HttpMethod.GET)
                .send();
        Assert.assertTrue(response.getContentAsString().contains("u1111"));
        Assert.assertEquals(createJson("u1111"), jedis.get("P:0:u1111"));
        Assert.assertNotNull(jedis.get("C:0:u1111"));

        response = httpClient.newRequest("http://127.0.0.1:8080/linksrv/get")
                .header(HttpHeader.AUTHORIZATION, "authkey")
                .method(HttpMethod.GET)
                .send();
        Assert.assertEquals("", response.getContentAsString());

        response = httpClient.POST("http://localhost:8080/linksrv/delete")
                .header(HttpHeader.AUTHORIZATION, "authkey")
                .content(new StringContentProvider(createJson("u2222")), "application/json").send();
        Assert.assertEquals("true", response.getContentAsString());
        Assert.assertNull(jedis.get("P:0:u2222"));
        Assert.assertEquals("",jedis.get("C:0:u2222"));
    }

    @Test
    public void failAuthTest() throws Exception {
        ContentResponse response = httpClient.newRequest("http://127.0.0.1:8080/linksrv/get")
                .header(HttpHeader.AUTHORIZATION, "11111111111111")
                .method(HttpMethod.GET)
                .send();
        Assert.assertEquals(HttpStatus.FORBIDDEN_403, response.getStatus());
    }

    String createJson(String url) {
        return "{\"url\":\"" + url + "\",\"sequenceNum\":0,\"handlerName\":\"HH_RESUME\",\"props\":null}";
    }

}
