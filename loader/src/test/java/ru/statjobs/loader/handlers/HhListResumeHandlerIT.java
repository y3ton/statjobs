package ru.statjobs.loader.handlers;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.statjobs.loader.JsScript;
import ru.statjobs.loader.SeleniumBrowser;
import ru.statjobs.loader.dao.QueueDownloadableLinkDaoImpl;
import ru.statjobs.loader.testutils.H2Utils;
import ru.statjobs.loader.url.UrlConstructor;
import ru.statjobs.loader.utils.JsonUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

public class HhListResumeHandlerIT {

    private static Connection connection;
    private static WireMockServer wireMockServer = new WireMockServer();

    private static QueueDownloadableLinkDaoImpl dao;
    private static SeleniumBrowser browser;
    private static JsScript jsScript;
    private static UrlConstructor urlConstructor;
    private static JsonUtils jsonUtils;



    @BeforeClass
    public static void start() throws SQLException {
        connection = DriverManager.getConnection ("jdbc:h2:mem:test;MODE=PostgreSQL");
        jsonUtils = new JsonUtils();
        browser = mock(SeleniumBrowser.class);
        jsScript = mock(JsScript.class);
        H2Utils.runScript("sql/queue.sql", connection);
        dao = new QueueDownloadableLinkDaoImpl(connection, jsonUtils, false);
        urlConstructor = new UrlConstructor();
        wireMockServer.start();
    }

    @AfterClass
    public static void stop() throws SQLException {
        connection.close();
        wireMockServer.stop();
    }

    @Test
    public void processTest() {
        HhListResumeHandler handler = new HhListResumeHandler(browser, jsScript, dao, urlConstructor);
        doReturn(false).when(browser).isStart();

        verify(browser, times(1)).start();
    }


}
