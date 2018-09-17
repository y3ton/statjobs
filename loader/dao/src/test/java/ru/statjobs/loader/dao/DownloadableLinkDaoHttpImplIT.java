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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadableLinkDaoHttpImplIT {

    private final static JsonUtils jsonUtils = new JsonUtils();

    private final static DownloadableLinkDaoHttpImpl daoHttp = new DownloadableLinkDaoHttpImpl(jsonUtils, "http://127.0.0.1:18080/linksrv", "");
    private final static Server server = new Server(18080);

    Map<String, String> map =  new ConcurrentHashMap<>();

    @BeforeClass
    public static void start() throws Exception {
        daoHttp.start();
    }

    @AfterClass
    public static void stop() throws Exception {
        daoHttp.stop();
        server.stop();
    }

    @Test
    public void successTest() throws Exception {

        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                String json = request.getReader().readLine();
                map.put(request.getHttpURI().toString(), json == null ? "" : json);
                request.setHandled(true);
            }
        });
        server.start();

        daoHttp.createDownloadableLink(new DownloadableLink("urlCREATE1", 1, UrlTypes.HH_LIST_RESUME, null));
        daoHttp.deleteDownloadableLink(new DownloadableLink("urlDELETE1", 1, UrlTypes.HH_LIST_RESUME, null));
        daoHttp.getDownloadableLink();

        Assert.assertTrue(map.containsKey("//127.0.0.1:18080/linksrv/get"));
        Assert.assertTrue(map.get("//127.0.0.1:18080/linksrv/create").contains("urlCREATE1"));
        Assert.assertTrue(map.get("//127.0.0.1:18080/linksrv/delete").contains("urlDELETE1"));
    }


}
