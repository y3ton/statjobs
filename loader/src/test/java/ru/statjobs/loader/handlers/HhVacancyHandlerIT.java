package ru.statjobs.loader.handlers;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.statjobs.loader.dao.QueueDownloadableLinkDaoImpl;
import ru.statjobs.loader.dao.RawDataStorageDao;
import ru.statjobs.loader.dao.RawDataStorageDaoImpl;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.testutils.H2Utils;
import ru.statjobs.loader.url.UrlHandler;
import ru.statjobs.loader.utils.Downloader;
import ru.statjobs.loader.utils.JsonUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class HhVacancyHandlerIT {

    private static Connection connection;
    private static WireMockServer wireMockServer = new WireMockServer();

    private static QueueDownloadableLinkDaoImpl dao;
    private static Downloader downloader;
    private static RawDataStorageDao rawDataStorageDao;

    @Before
    public void start() throws SQLException {
        connection = DriverManager.getConnection ("jdbc:h2:mem:test;MODE=PostgreSQL");
        H2Utils.runScript("sql/queue.sql", connection);
        H2Utils.runScript("sql/raw.sql", connection);
        dao = new QueueDownloadableLinkDaoImpl(connection, new JsonUtils(), false);

        downloader = new Downloader();
        rawDataStorageDao = spy(new RawDataStorageDaoImpl(connection, false));
        wireMockServer.start();
    }

    @After
    public void stop() throws SQLException {
        connection.close();
        wireMockServer.stop();
    }

    @Test
    public void hhVacancyHandlerComplexTest() throws SQLException {
        HhVacancyHandler handler = new HhVacancyHandler(downloader, rawDataStorageDao, dao);
        DownloadableLink dl = new DownloadableLink("http://localhost:8080/json1", 181, UrlHandler.HH_VACANCY.name(), null);
        dao.createDownloadableLink(dl);
        Assert.assertNotNull(dao.getDownloadableLink());

        stubFor(get(urlEqualTo("/json1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody("{[]}")));
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
    public void hhVacancyHandler404Test() throws SQLException {
        HhVacancyHandler handler = new HhVacancyHandler(downloader, rawDataStorageDao, dao);
        DownloadableLink dl = new DownloadableLink("http://localhost:8080/json1", 181, UrlHandler.HH_VACANCY.name(), null);
        dao.createDownloadableLink(dl);
        Assert.assertNotNull(dao.getDownloadableLink());

        stubFor(get(urlEqualTo("/json1"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "text/xml")
                        .withBody("{[]}")));
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
    public void hhVacancyHandler500Test() throws SQLException {
        HhVacancyHandler handler = new HhVacancyHandler(downloader, rawDataStorageDao, dao);
        DownloadableLink dl = new DownloadableLink("http://localhost:8080/json1", 181, UrlHandler.HH_VACANCY.name(), null);
        dao.createDownloadableLink(dl);
        Assert.assertNotNull(dao.getDownloadableLink());

        stubFor(get(urlEqualTo("/json1"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "text/xml")
                        .withBody("{[]}")));
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
