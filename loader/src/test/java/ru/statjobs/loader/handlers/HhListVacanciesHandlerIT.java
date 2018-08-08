package ru.statjobs.loader.handlers;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.*;
import ru.statjobs.loader.dao.DownloadableLinkDao;
import ru.statjobs.loader.dao.DownloadableLinkDaoPostgresImpl;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.testutils.H2Utils;
import ru.statjobs.loader.url.UrlConstructor;
import ru.statjobs.loader.url.UrlHandler;
import ru.statjobs.loader.utils.Downloader;
import ru.statjobs.loader.utils.JsonUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class HhListVacanciesHandlerIT {

    private static Connection connection;
    private static WireMockServer wireMockServer = new WireMockServer();

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
        wireMockServer.start();
    }

    @AfterClass
    public static void stop() throws SQLException {
        connection.close();
        wireMockServer.stop();
    }

    @After
    public void clean() {
        try (Statement statement = connection.createStatement();){
            statement.execute("delete from T_QUEUE_DOWNLOADABLE_LINK");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void hhListVacanciesHandlerComplexTest() {
        HhListVacanciesHandler handler = new HhListVacanciesHandler(downloader, jsonUtils, dao, urlConstructor);

        DownloadableLink link = new DownloadableLink("http://localhost:8080/json1", 1, UrlHandler.HH_LIST_VACANCIES.name(), null);
        dao.createDownloadableLink(link);
        Assert.assertNotNull(dao.getDownloadableLink());

        stubFor(get(urlEqualTo("/json1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        // double url1
                        .withBody("{\"items\": [{\"url\": \"url1\"},{\"url\": \"url2\"},{\"url\": \"url1\"}]}")));

        handler.process(link);
        Map<String, DownloadableLink> map = IntStream.rangeClosed(0, 2)
                .mapToObj(i -> dao.getDownloadableLink())
                .collect(Collectors.toMap(DownloadableLink::getUrl, x -> x));
        Assert.assertEquals(UrlHandler.HH_VACANCY.name(), map.get("url1").getHandlerName());
        Assert.assertEquals(UrlHandler.HH_VACANCY.name(), map.get("url2").getHandlerName());
        Assert.assertEquals(UrlHandler.HH_LIST_VACANCIES.name(), map.get("http://localhost:8080/json1&page=1").getHandlerName());
        Assert.assertNull(dao.getDownloadableLink());
    }

    @Test
    public void hhListVacanciesHandlerDownloadEmptyResultTest() {
        HhListVacanciesHandler handler = new HhListVacanciesHandler(downloader, jsonUtils, dao, urlConstructor);

        DownloadableLink link = new DownloadableLink("http://localhost:8080/json1", 1, UrlHandler.HH_LIST_VACANCIES.name(), null);
        dao.createDownloadableLink(link);
        Assert.assertNotNull(dao.getDownloadableLink());

        stubFor(get(urlEqualTo("/json1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        // double url1
                        .withBody("{\"items\":[]}")));

        handler.process(link);
        Assert.assertNull(dao.getDownloadableLink());
    }

}
