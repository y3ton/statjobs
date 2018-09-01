package ru.statjobs.loader.utils;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DownloaderIT  {

    Downloader downloader = new Downloader();


    private static Server jettyServer = new Server(8080);

    @After
    public void stop() throws Exception {
        jettyServer.stop();
    }

    @Test
    public void test200() throws Exception {
        jettyServer.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                request.setHandled(true);
                httpServletResponse.setContentType("text/xml");
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                httpServletResponse.getWriter().print("ok!");
            }
        });
        jettyServer.start();
        Downloader.DownloaderResult result = downloader.download("http://localhost:8080/test200", StandardCharsets.UTF_8, 1000);
        Assert.assertEquals("ok!", result.getText());
        Assert.assertEquals((Integer)200, result.getResponseCode());
    }

    @Test
    public void test404() throws Exception {
        jettyServer.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                request.setHandled(true);
                httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        });
        jettyServer.start();
        Downloader.DownloaderResult result = downloader.download("http://localhost:8080/test404", StandardCharsets.UTF_8, 1000);
        Assert.assertEquals((Integer)404, result.getResponseCode());
    }

    @Test()
    public void testTimeoutException() throws Exception {
        jettyServer.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                request.setHandled(true);
                httpServletResponse.setContentType("text/xml");
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                httpServletResponse.getWriter().println("ok!");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        jettyServer.start();
        try {
            downloader.download("http://localhost:8080/delayed", StandardCharsets.UTF_8, 100);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().toLowerCase().contains("timeout"));
            return;
        }
        Assert.fail();
    }

}