package ru.statjobs.loader.dao;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.*;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.url.UrlTypes;
import ru.statjobs.loader.utils.JsonUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RawDataStorageDaoHttpImplIT {

    private final static JsonUtils jsonUtils = new JsonUtils();

    private final static RawDataStorageDaoHttpImpl daoHttp = new RawDataStorageDaoHttpImpl(jsonUtils, "http://127.0.0.1:18080/", "key");
    private final static Server server = new Server(18080);

    @BeforeClass
    public static void start() throws Exception {
        daoHttp.start();
    }

    @AfterClass
    public static void stop() throws Exception {
        daoHttp.stop();
    }

    @After
    public void endTest() throws Exception {
        server.stop();
    }

    @Test
    public void test500() throws Exception {
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                request.setHandled(true);
                httpServletResponse.setStatus(500);
            }
        });
        server.start();
        try {
            daoHttp.saveHhVacancy(new DownloadableLink("123", 1, UrlTypes.HH_LIST_RESUME, null), "{}");
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("500"));
        }
    }

    @Test(expected = Exception.class)
    public void testStopServer() throws Exception {
        daoHttp.saveHhVacancy(new DownloadableLink("123", 1, UrlTypes.HH_LIST_RESUME, null), "{}");
    }

    @Test
    public void successTest() throws Exception {
        final String[] arr = {""};
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                arr[0] = request.getReader().readLine();
                request.setHandled(true);
            }
        });
        server.start();
        daoHttp.saveHhVacancy(new DownloadableLink("123", 1, UrlTypes.HH_LIST_RESUME, null), "{\"a3\":\"b3\"}");
        Assert.assertEquals(
        "{\"link\":{\"url\":\"123\",\"sequenceNum\":1,\"handlerName\":\"HH_LIST_RESUME\",\"props\":null},\"json\":\"{\\\"a3\\\":\\\"b3\\\"}\"}",
                arr[0]
        );
    }

}
