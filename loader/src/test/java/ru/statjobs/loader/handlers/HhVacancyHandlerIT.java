package ru.statjobs.loader.handlers;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import ru.statjobs.loader.UrlHandler;
import ru.statjobs.loader.dao.QueueDownloadableLinkDaoImpl;
import ru.statjobs.loader.dao.RawDataStorageDao;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.testutils.H2Utils;
import ru.statjobs.loader.utils.Downloader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class HhVacancyHandlerIT {

    private static Connection connection;
    private static WireMockServer wireMockServer = new WireMockServer();

    private static QueueDownloadableLinkDaoImpl dao;
    private static Downloader downloader;
    private static RawDataStorageDao rawDataStorageDao;

    @BeforeClass
    public static void start() throws SQLException {
        connection = DriverManager.getConnection ("jdbc:h2:mem:test;MODE=PostgreSQL");
        H2Utils.runScript("sql/queue.sql", connection);
        dao = new QueueDownloadableLinkDaoImpl(connection);
        downloader = new Downloader();
        rawDataStorageDao = Mockito.mock(RawDataStorageDao.class);
        wireMockServer.start();
    }

    @AfterClass
    public static void stop() throws SQLException {
        connection.close();
        wireMockServer.stop();
    }

    @Test
    public void hhVacancyHandlerComplexTest() {
        HhVacancyHandler handler = new HhVacancyHandler(downloader, rawDataStorageDao, dao);
        DownloadableLink dl = new DownloadableLink("http://localhost:8080/json1", 1, UrlHandler.HH_VACANCY.name());
        dao.createDownloadableLink(dl);
        Assert.assertNotNull(dao.getDownloadableLink());

        stubFor(get(urlEqualTo("/json1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody("{[]}")));
        handler.process(dl);

        Mockito.verify(
                rawDataStorageDao,
                Mockito.times(1)).saveHhVacancy(Mockito.anyObject(), Mockito.eq("{[]}")
        );
        Assert.assertFalse(dao.deleteDownloadableLink(dl));
    }

}
