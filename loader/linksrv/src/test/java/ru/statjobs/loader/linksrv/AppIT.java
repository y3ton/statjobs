package ru.statjobs.loader.linksrv;

import ai.grakn.redismock.RedisServer;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.junit.*;
import redis.clients.jedis.Jedis;

public class AppIT {

    Thread thread;
    RedisServer redisServer;
    HttpClient httpClient;
    Jedis jedis;

    @Before
    public void init() throws Exception {
        redisServer = RedisServer.newRedisServer();
        redisServer.start();
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                App.process("127.0.0.1", redisServer.getBindPort());
            }
        });
        thread.start();
        httpClient = new HttpClient();
        httpClient.start();
        jedis = new Jedis("127.0.0.1", redisServer.getBindPort());
        Thread.sleep(5000);
    }

    @After
    public void close() throws Exception {
        thread.interrupt();
        httpClient.stop();
        redisServer.stop();
        jedis.close();
    }

    @Test
    public void complexTest() throws Exception {

        ContentResponse response;

        response = httpClient.GET("http://127.0.0.1:8080/linksrv/get");
        Assert.assertEquals("", response.getContentAsString());

        response = httpClient.POST("http://localhost:8080/linksrv/create")
            .content(new StringContentProvider(createJson("u1111")), "application/json").send();
        Assert.assertEquals("true", response.getContentAsString());

        response = httpClient.POST("http://localhost:8080/linksrv/create")
                .content(new StringContentProvider(createJson("u1111")), "application/json").send();
        Assert.assertEquals("true", response.getContentAsString());

        response = httpClient.POST("http://localhost:8080/linksrv/create")
                .content(new StringContentProvider(createJson("u2222")), "application/json").send();
        Assert.assertEquals("true", response.getContentAsString());

        response = httpClient.GET("http://127.0.0.1:8080/linksrv/get");
        Assert.assertTrue(response.getContentAsString().contains("u2222"));
        response = httpClient.GET("http://127.0.0.1:8080/linksrv/get");
        Assert.assertTrue(response.getContentAsString().contains("u1111"));
        response = httpClient.GET("http://127.0.0.1:8080/linksrv/get");
        Assert.assertEquals("", response.getContentAsString());

        response = httpClient.POST("http://localhost:8080/linksrv/delete")
                .content(new StringContentProvider(createJson("u2222")), "application/json").send();
        Assert.assertEquals("true", response.getContentAsString());
        Assert.assertEquals("CREATE", jedis.get("0:u1111"));
        Assert.assertEquals("DELETE",jedis.get("0:u2222"));
    }

    String createJson(String url) {
        return "{\"url\":\"" + url + "\",\"sequenceNum\":0,\"handlerName\":\"HH_RESUME\",\"props\":null},\"json\":\"{}\"}";
    }

}
