package ru.statjobs.loader.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadableLinkDaoHttpImplIT {

    private final static JsonUtils jsonUtils = new JsonUtils();
    private final static HttpUtils httpUtils = new HttpUtils();

    private final static DownloadableLinkDaoHttpImpl daoHttp = new DownloadableLinkDaoHttpImpl(httpUtils, jsonUtils, "http://127.0.0.1:18080/linksrv", "");
    private final static Server server = new Server(18080);

    Map<String, String> map =  new ConcurrentHashMap<>();

    @Before
    public void start() throws Exception {
        daoHttp.start();
    }

    @After
    public void stop() throws Exception {
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

    @Test
    public void batchCreateTest() throws Exception {
        Map<String, DownloadableLink> map = new ConcurrentHashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        AtomicInteger countRequest = new AtomicInteger(0);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                List<DownloadableLink> list = mapper.readValue (request.getReader().readLine(), new TypeReference<List<DownloadableLink>>(){});
                for (DownloadableLink link: list) {
                    map.put(link.getUrl(), link);
                }
                countRequest.incrementAndGet();
                request.setHandled(true);
            }
        });
        server.start();
        List<DownloadableLink> list = new ArrayList<>();
        for (int i = 0; i < 223; i++) {
            list.add(new DownloadableLink("u" + i, 333, UrlTypes.HH_RESUME, null));
        }
        daoHttp.createDownloadableLinks(list);
        Assert.assertEquals(223, map.keySet().size());
        Assert.assertEquals(3, countRequest.get());
        for (int i = 0; i < 223; i++) {
            Assert.assertEquals("u" + i, map.get("u" + i).getUrl());
        }


    }


}
