package ru.statjobs.loader.handlers;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.*;
import ru.statjobs.loader.common.dao.DownloadableLinkDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.url.UrlTypes;
import ru.statjobs.loader.dao.DownloadableLinkDaoPostgresImpl;
import ru.statjobs.loader.testutils.H2Utils;
import ru.statjobs.loader.url.UrlConstructor;
import ru.statjobs.loader.utils.Downloader;
import ru.statjobs.loader.utils.JsonUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;



public class HhListVacanciesHandlerIT {

    private static Connection connection;
    private static Server jettyServer = new Server(8080);

    private static DownloadableLinkDao dao;
    private static Downloader downloader;
    private static UrlConstructor urlConstructor;
    private static JsonUtils jsonUtils;

    @BeforeClass
    public static void start() throws SQLException {
        connection = DriverManager.getConnection ("jdbc:h2:mem:test;MODE=PostgreSQL");
        jsonUtils = new JsonUtils();
        H2Utils.runScript("sql/queue.sql", connection);
        dao = new DownloadableLinkDaoPostgresImpl(connection, jsonUtils, false);
        downloader = new Downloader();
        urlConstructor = new UrlConstructor();
    }

    @AfterClass
    public static void stop() throws Exception {
        connection.close();
    }

    @After
    public void clean() throws Exception {
        jettyServer.stop();
        try (Statement statement = connection.createStatement();){
            statement.execute("delete from T_QUEUE_DOWNLOADABLE_LINK");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void hhListVacanciesHandlerComplexTest() throws Exception {
        HhListVacanciesHandler handler = new HhListVacanciesHandler(downloader, jsonUtils, dao, urlConstructor);

        DownloadableLink link = new DownloadableLink("http://localhost:8080/json1", 1, UrlTypes.HH_LIST_VACANCIES, null);
        dao.createDownloadableLink(link);
        Assert.assertNotNull(dao.getDownloadableLink());
        jettyServer.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                if (!s.equals("/json1")) {
                    throw new RuntimeException("wrong url " + s);
                }
                httpServletResponse.setContentType("text/xml");
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                httpServletResponse.getWriter().print("{\"items\": [{\"url\": \"url1\"},{\"url\": \"url2\"},{\"url\": \"url1\"}]}");
                request.setHandled(true);

            }
        });
        jettyServer.start();

        handler.process(link);
        Map<String, DownloadableLink> map = IntStream.rangeClosed(0, 2)
                .mapToObj(i -> dao.getDownloadableLink())
                .collect(Collectors.toMap(DownloadableLink::getUrl, x -> x));
        Assert.assertEquals(UrlTypes.HH_VACANCY, map.get("url1").getHandlerName());
        Assert.assertEquals(UrlTypes.HH_VACANCY, map.get("url2").getHandlerName());
        Assert.assertEquals(UrlTypes.HH_LIST_VACANCIES, map.get("http://localhost:8080/json1&page=1").getHandlerName());
        Assert.assertNull(dao.getDownloadableLink());
    }

    @Test
    public void hhListVacanciesHandlerDownloadEmptyResultTest() throws Exception {
        HhListVacanciesHandler handler = new HhListVacanciesHandler(downloader, jsonUtils, dao, urlConstructor);

        DownloadableLink link = new DownloadableLink("http://localhost:8080/json1", 1, UrlTypes.HH_LIST_VACANCIES, null);
        dao.createDownloadableLink(link);
        Assert.assertNotNull(dao.getDownloadableLink());

        jettyServer.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                if (!s.equals("/json1")) {
                    throw new RuntimeException("wrong url " + s);
                }
                httpServletResponse.setContentType("text/xml");
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                httpServletResponse.getWriter().print("{\"items\":[]}");
                request.setHandled(true);

            }
        });
        jettyServer.start();

        handler.process(link);
        Assert.assertNull(dao.getDownloadableLink());
    }

}
