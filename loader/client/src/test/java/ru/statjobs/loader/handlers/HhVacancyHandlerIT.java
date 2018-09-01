package ru.statjobs.loader.handlers;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.statjobs.loader.common.dao.DownloadableLinkDao;
import ru.statjobs.loader.common.dao.RawDataStorageDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.url.UrlTypes;
import ru.statjobs.loader.dao.DownloadableLinkDaoPostgresImpl;
import ru.statjobs.loader.dao.RawDataStorageDaoPostgresImpl;
import ru.statjobs.loader.testutils.H2Utils;
import ru.statjobs.loader.utils.Downloader;
import ru.statjobs.loader.utils.JsonUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class HhVacancyHandlerIT {

    private static Connection connection;
    private static Server jettyServer = new Server(8080);
    private static JsonUtils jsonUtils = new JsonUtils();

    private static DownloadableLinkDao dao;
    private static Downloader downloader;
    private static RawDataStorageDao rawDataStorageDao;

    @Before
    public void start() throws SQLException {
        connection = DriverManager.getConnection ("jdbc:h2:mem:test;MODE=PostgreSQL");
        H2Utils.runScript("sql/queue.sql", connection);
        H2Utils.runScript("sql/raw.sql", connection);
        dao = new DownloadableLinkDaoPostgresImpl(connection, new JsonUtils(), false);

        downloader = new Downloader();
        rawDataStorageDao = spy(new RawDataStorageDaoPostgresImpl(connection, false));
    }

    @After
    public void stop() throws Exception {
        connection.close();
        jettyServer.stop();
    }

    @Test
    public void hhVacancyHandlerComplexTest() throws Exception {
        HhVacancyHandler handler = new HhVacancyHandler(downloader, rawDataStorageDao, dao, jsonUtils);
        DownloadableLink dl = new DownloadableLink("http://localhost:8080/json1", 181, UrlTypes.HH_VACANCY, null);
        dao.createDownloadableLink(dl);
        Assert.assertNotNull(dao.getDownloadableLink());
        jettyServer.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                if (!s.equals("/json1")) {
                    throw new RuntimeException("wrong url " + s);
                }
                httpServletResponse.setContentType("text/xml");
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                httpServletResponse.getWriter().print("{[]}");
                request.setHandled(true);

            }
        });
        jettyServer.start();

        handler.process(dl);

        verify(
                rawDataStorageDao,
                times(1)).saveHhVacancy(anyObject(), eq("{[]}")
        );
        Assert.assertFalse(dao.deleteDownloadableLink(dl));

        ResultSet resultSet = connection.createStatement().executeQuery("select URL, DATA, SEQUENCE_NUM from T_HH_RAW_VACANCIES");
        Assert.assertTrue(resultSet.next());

        Assert.assertEquals("http://localhost:8080/json1", resultSet.getString("URL"));
        Assert.assertEquals("{[]}", resultSet.getString("DATA"));
        Assert.assertEquals(181, resultSet.getInt("SEQUENCE_NUM"));

        Assert.assertFalse(resultSet.next());
        resultSet.close();
    }

    @Test
    public void hhVacancyHandler404Test() throws Exception {
        HhVacancyHandler handler = new HhVacancyHandler(downloader, rawDataStorageDao, dao, jsonUtils);
        DownloadableLink dl = new DownloadableLink("http://localhost:8080/json1", 181, UrlTypes.HH_VACANCY, null);
        dao.createDownloadableLink(dl);
        Assert.assertNotNull(dao.getDownloadableLink());
        jettyServer.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                if (!s.equals("/json1")) {
                    throw new RuntimeException("wrong url " + s);
                }
                httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                request.setHandled(true);
            }
        });
        jettyServer.start();

        handler.process(dl);

        verify(
                rawDataStorageDao,
                times(0)).saveHhVacancy(anyObject(), anyString()
        );
        Assert.assertFalse(dao.deleteDownloadableLink(dl));

        ResultSet resultSet = connection.createStatement().executeQuery("select URL, DATA, SEQUENCE_NUM from T_HH_RAW_VACANCIES");
        Assert.assertFalse(resultSet.next());
        resultSet.close();
    }

    @Test
    public void hhVacancyHandler500Test() throws Exception {
        HhVacancyHandler handler = new HhVacancyHandler(downloader, rawDataStorageDao, dao, jsonUtils);
        DownloadableLink dl = new DownloadableLink("http://localhost:8080/json1", 181, UrlTypes.HH_VACANCY, null);
        dao.createDownloadableLink(dl);
        Assert.assertNotNull(dao.getDownloadableLink());
        jettyServer.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                if (!s.equals("/json1")) {
                    throw new RuntimeException("wrong url " + s);
                }
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                request.setHandled(true);
            }
        });
        jettyServer.start();
        boolean except = false;
        try {
            handler.process(dl);
        } catch (Exception ex) {
            except = true;
        }
        Assert.assertTrue(except);

        verify(
                rawDataStorageDao,
                times(0)).saveHhVacancy(anyObject(), anyString()
        );
        Assert.assertTrue(dao.deleteDownloadableLink(dl));

        ResultSet resultSet = connection.createStatement().executeQuery("select URL, DATA, SEQUENCE_NUM from T_HH_RAW_VACANCIES");
        Assert.assertFalse(resultSet.next());
        resultSet.close();
    }
}
