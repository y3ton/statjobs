package ru.statjobs.loader.dao;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpUtilsIT {

    private final static Server server = new Server(18080);
    private final static HttpClient httpClient = new HttpClient();
    private final static HttpUtils httpUtils = new HttpUtils();

    @Before
    public void init() throws Exception {
        httpClient.start();
    }

    @After
    public void stop() throws Exception {
        server.stop();
        httpClient.stop();
    }

    @Test
    public void successTest() throws Exception {
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                httpServletResponse.getOutputStream().print(request.getReader().readLine());
                request.setHandled(true);
            }
        });
        server.start();
        org.eclipse.jetty.client.api.Request request = httpClient.POST("http://127.0.0.1:18080")
                .content(new StringContentProvider("sucessTest"), "application/json");

        Assert.assertEquals("sucessTest", httpUtils.send(
                () -> httpClient.POST("http://127.0.0.1:18080").content(new StringContentProvider("sucessTest"),"application/json"),
                1));
    }

    @Test
    public void resendTest() throws Exception {
        AtomicInteger a = new AtomicInteger(0);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                if (a.incrementAndGet() == 3) {
                    httpServletResponse.setStatus(200);
                    httpServletResponse.getWriter().print(a.get());
                } else {
                    httpServletResponse.setStatus(500);
                }
                request.setHandled(true);
            }
        });
        server.start();
        org.eclipse.jetty.client.api.Request request = httpClient
                .newRequest("http://127.0.0.1:18080")
                .method(HttpMethod.GET);
        long l = System.currentTimeMillis();

        Assert.assertEquals(
                "3",
                httpUtils.send(() -> httpClient.POST("http://127.0.0.1:18080"), 3));
        l = System.currentTimeMillis() - l;
        Assert.assertTrue(l >= 3000);
        Assert.assertTrue(l < 4000);
        try {
            Assert.assertEquals(
                    "3",
                    httpUtils.send(() -> httpClient.POST("http://127.0.0.1:18080"), 1));
            Assert.fail();
        } catch(Exception e) {
            // ok
        }
    }


}
