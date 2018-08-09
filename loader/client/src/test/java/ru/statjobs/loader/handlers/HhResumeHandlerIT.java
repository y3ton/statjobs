package ru.statjobs.loader.handlers;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.statjobs.loader.Const;
import ru.statjobs.loader.JsScript;
import ru.statjobs.loader.SeleniumBrowser;
import ru.statjobs.loader.common.dao.DownloadableLinkDao;
import ru.statjobs.loader.common.dao.RawDataStorageDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.dao.DownloadableLinkDaoPostgresImpl;
import ru.statjobs.loader.dao.RawDataStorageDaoPostgresImpl;
import ru.statjobs.loader.testutils.H2Utils;
import ru.statjobs.loader.url.UrlHandler;
import ru.statjobs.loader.utils.JsonUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class HhResumeHandlerIT {


    private static Connection connection;
    private static DownloadableLinkDao dao;
    private static RawDataStorageDao rawDataStorageDao;
    private static SeleniumBrowser browser;
    private static JsScript jsScript;

    @BeforeClass
    public static void start() throws SQLException {
        connection = DriverManager.getConnection ("jdbc:h2:mem:test;MODE=PostgreSQL");
        H2Utils.runScript("sql/queue.sql", connection);
        H2Utils.runScript("sql/raw.sql", connection);
        dao = new DownloadableLinkDaoPostgresImpl(connection, new JsonUtils(), false);
        rawDataStorageDao = spy(new RawDataStorageDaoPostgresImpl(connection, false));
        jsScript = mock(JsScript.class);
        browser = mock(SeleniumBrowser.class);
    }

    @AfterClass
    public static void stop() throws SQLException {
        connection.close();
    }

    @Test
    public void hhVacancyHandlerComplexTest() throws SQLException {
        HhResumeHandler handler = new HhResumeHandler(browser, jsScript, rawDataStorageDao, dao);

        dao.createDownloadableLink(
                new DownloadableLink("url17", 17, UrlHandler.HH_LIST_RESUME.name(),
                new HashMap<String, String>() {{
                    put(Const.AREA_CODE, "area1");
                    put(Const.DATE_CREATE_RESUME, "date1");
                }}));
        DownloadableLink initLink = dao.getDownloadableLink();

        doReturn(false).when(browser).isStart();
        doReturn("{\"asd\": \"asd\"}").when(browser).execJs(anyString());

        handler.process(initLink);
        String json = "{\"dateCreate\":\"date1\",\"area\":\"area1\",\"asd\": \"asd\"}";

        verify(
                rawDataStorageDao,
                times(1)).saveHhResume(anyObject(), eq(json)
        );

        Assert.assertNull(dao.getDownloadableLink());
        Assert.assertFalse(dao.deleteDownloadableLink(initLink));

        ResultSet resultSet = connection.createStatement().executeQuery("select URL, DATA, SEQUENCE_NUM from T_HH_RAW_RESUMES");
        Assert.assertTrue(resultSet.next());

        Assert.assertEquals("url17", resultSet.getString("URL"));
        Assert.assertEquals(json, resultSet.getString("DATA"));
        Assert.assertEquals(17, resultSet.getInt("SEQUENCE_NUM"));

        Assert.assertFalse(resultSet.next());
        resultSet.close();

    }

}
