package ru.statjobs.loader.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class DownloaderIT  {

    Downloader downloader = new Downloader();


    private static WireMockServer wireMockServer = new WireMockServer();

    @BeforeClass
    public static void start() throws IOException {
        wireMockServer.start();

    }

    @AfterClass
    public static void stop() {
        wireMockServer.stop();
    }

    @Test
    public void test200() throws InterruptedException {
        stubFor(get(urlEqualTo("/test200"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody("ok!")));
        Downloader.DownloaderResult result = downloader.download("http://localhost:8080/test200", StandardCharsets.UTF_8, 1000);
        Assert.assertEquals("ok!", result.getText());
        Assert.assertEquals((Integer)200, result.getResponseCode());
    }

    @Test
    public void test404() throws InterruptedException {
        Downloader.DownloaderResult result = downloader.download("http://localhost:8080/test404", StandardCharsets.UTF_8, 1000);
        Assert.assertEquals((Integer)404, result.getResponseCode());
    }

    @Test(expected = java.lang.RuntimeException.class)
    public void testTimeoutException() throws InterruptedException {
        stubFor(get("/delayed").willReturn(
                aResponse()
                        .withStatus(200)
                        .withBody("ok!")
                        .withChunkedDribbleDelay(5, 1000)));
        downloader.download("http://localhost:8080/delayed", StandardCharsets.UTF_8, 100);
    }





}