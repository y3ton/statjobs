package ru.statjobs.loader;

import org.junit.*;
import ru.statjobs.loader.common.dao.DownloadableLinkDao;
import ru.statjobs.loader.common.dao.RawDataStorageDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.dao.DownloadableLinkDaoPostgresImpl;
import ru.statjobs.loader.testutils.H2Utils;
import ru.statjobs.loader.url.UrlConstructor;
import ru.statjobs.loader.url.UrlHandler;
import ru.statjobs.loader.utils.Downloader;
import ru.statjobs.loader.utils.FileUtils;
import ru.statjobs.loader.utils.JsonUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class LinkProcessorIT {

    private static UrlConstructor urlConstructor = new UrlConstructor();;
    private static JsonUtils jsonUtils = new JsonUtils();
    private static JsScript jsScript = new JsScript(new FileUtils());

    private static DownloadableLinkDao dao;

    private static Downloader downloader;
    private static RawDataStorageDao rawDataStorageDao;
    private static SeleniumBrowser seleniumBrowser;


    private static Connection connection;
    private static LinkProcessor processor;

    private static final String JSON = "{\"aa\":\"aa\"}";
    private static final String JSON_INS = "{\"dateCreate\":\"null\",\"area\":\"null\",\"aa\":\"aa\"}";

    @BeforeClass
    public static void start() throws SQLException {
        connection = DriverManager.getConnection ("jdbc:h2:mem:test;MODE=PostgreSQL");
        H2Utils.runScript("sql/queue.sql", connection);
        dao = spy(new DownloadableLinkDaoPostgresImpl(connection,  new JsonUtils(), false));

    }

    @AfterClass
    public static void stop() throws SQLException {
        connection.close();
    }

    @Before
    public void init() {
        downloader = mock(Downloader.class);
        rawDataStorageDao = mock(RawDataStorageDao.class);
        seleniumBrowser = mock(SeleniumBrowser.class);
        processor = new LinkProcessor(downloader, urlConstructor,  jsonUtils, dao, rawDataStorageDao,  seleniumBrowser, jsScript);
    }

    @Test
    public void hhResumeHandlerEmptyTest() {
        dao.createDownloadableLink(new DownloadableLink("hhResumeHandlerEmptyTest", 1, UrlHandler.HH_RESUME.name(), null));
        doReturn("").when(seleniumBrowser).execJs(anyString());
        try {
            processor.processLinks();
        } catch (Exception ex) {
            Assert.assertTrue(ex.getMessage().startsWith("fail load resume"));
            Assert.assertNull(dao.getDownloadableLink());
            return;
        }
        Assert.fail();

    }

    @Test
    public void hhResumeHandlerTest() {
        dao.createDownloadableLink(new DownloadableLink("hhResumeHandlerTest", 1, UrlHandler.HH_RESUME.name(), null));
        doReturn(JSON).when(seleniumBrowser).execJs(anyString());
        processor.processLinks();
        verify(seleniumBrowser, times(1)).execJs(anyString());
        verify(rawDataStorageDao, times(1)).saveHhResume(anyObject(), eq(JSON_INS));
        Assert.assertNull(dao.getDownloadableLink());
    }

    @Test
    public void hhVacancyHandlerTest() {
        dao.createDownloadableLink(new DownloadableLink("hhVacancyHandlerTest", 1, UrlHandler.HH_VACANCY.name(), null));
        Downloader.DownloaderResult result = mock(Downloader.DownloaderResult.class);
        doReturn(result).when(downloader).download(eq("hhVacancyHandlerTest"), anyObject(), anyInt());
        doReturn(200).when(result).getResponseCode();
        doReturn("A").when(result).getText();
        processor.processLinks();
        verify(rawDataStorageDao, times(1)).saveHhVacancy(anyObject(), eq("A"));
        Assert.assertNull(dao.getDownloadableLink());
    }

    @Test
    public void hhVacancyHandlerEmptyTest() {
        dao.createDownloadableLink(new DownloadableLink("hhVacancyHandlerEmptyTest", 1, UrlHandler.HH_VACANCY.name(), null));
        Downloader.DownloaderResult result = mock(Downloader.DownloaderResult.class);
        doReturn(result).when(downloader).download(eq("hhVacancyHandlerEmptyTest"), anyObject(), anyInt());
        doReturn(501).when(result).getResponseCode();
        try {
            processor.processLinks();
        } catch (Exception ex) {
            Assert.assertTrue(ex.getMessage().startsWith("fail load url"));
            Assert.assertTrue(ex.getMessage().contains("501"));
            Assert.assertNull(dao.getDownloadableLink());
            return;
        }
        Assert.fail();

    }

    @Test
    public void hhListVacanciesHandlerTest() {
        dao.createDownloadableLink(new DownloadableLink("hhListVacanciesHandlerTest", 1, UrlHandler.HH_LIST_VACANCIES.name(), null));
        Downloader.DownloaderResult result = mock(Downloader.DownloaderResult.class);
        doReturn(result).when(downloader).download(eq("hhListVacanciesHandlerTest"), anyObject(), anyInt());
        doReturn(200).when(result).getResponseCode();
        doReturn("{\n" +
                "  \"items\": [\n" +
                "    {\"url\": \"A\"},\n" +
                "    {\"url\": \"A\"},\n" +
                "    {\"url\": \"A\"},\n" +
                "    {\"url\": \"A\"},\n" +
                "    {\"url\": \"B\"}\n" +
                "  ]\n" +
                "}").when(result).getText();

        Downloader.DownloaderResult resultNext = mock(Downloader.DownloaderResult.class);
        doReturn(resultNext).when(downloader).download(eq("hhListVacanciesHandlerTest&page=1"), anyObject(), anyInt());
        doReturn(200).when(resultNext).getResponseCode();
        doReturn("{\"items\": []}").when(resultNext).getText();

        Downloader.DownloaderResult resultA = mock(Downloader.DownloaderResult.class);
        doReturn(resultA).when(downloader).download(eq("A"), anyObject(), anyInt());
        doReturn(200).when(resultA).getResponseCode();
        doReturn("A").when(resultA).getText();

        Downloader.DownloaderResult resultB = mock(Downloader.DownloaderResult.class);
        doReturn(resultB).when(downloader).download(eq("B"), anyObject(), anyInt());
        doReturn(200).when(resultB).getResponseCode();
        doReturn("B").when(resultB).getText();

        processor.processLinks();

        verify(rawDataStorageDao, times(1)).saveHhVacancy(anyObject(), eq("A"));
        verify(rawDataStorageDao, times(1)).saveHhVacancy(anyObject(), eq("B"));
        verify(downloader, times(1)).download(eq("hhListVacanciesHandlerTest&page=1"), anyObject(), anyInt());
        verify(downloader, times(1)).download(eq("hhListVacanciesHandlerTest"), anyObject(), anyInt());
        Assert.assertNull(dao.getDownloadableLink());
    }

    @Test
    public void hhListVacanciesHandlerEmptyTest() {
        dao.createDownloadableLink(new DownloadableLink("hhListVacanciesHandlerEmptyTest", 1, UrlHandler.HH_LIST_VACANCIES.name(), null));
        Downloader.DownloaderResult result = mock(Downloader.DownloaderResult.class);
        doReturn(result).when(downloader).download(eq("hhListVacanciesHandlerEmptyTest"), anyObject(), anyInt());
        doReturn(505).when(result).getResponseCode();
        doReturn("{}").when(result).getText();
        try {
            processor.processLinks();
        } catch (Exception ex) {
            Assert.assertTrue(ex.getMessage().startsWith("fail load url"));
            Assert.assertTrue(ex.getMessage().contains("505"));
            Assert.assertNull(dao.getDownloadableLink());
            return;
        }
        Assert.fail();

    }

    @Test
    public void hhListResumeHandlerTest() {
        List<List<String>> list = new ArrayList<List<String>>(){{
            add(new ArrayList<String>(){{
                add("hhListResumeHandlerTest1");
                add("Обновлено 1 января, 00:00");
            }});
        }};
        doReturn(0L).when(seleniumBrowser).execJs(eq("return document.querySelectorAll('[data-qa=\"pager-next\"]').length"));
        doReturn(list).when(seleniumBrowser).execJs(eq(jsScript.getResumeList()));
        doReturn(JSON).when(seleniumBrowser).execJs(eq(jsScript.getResume()));

        dao.createDownloadableLink(new DownloadableLink("hhListResumeHandlerTest", 1, UrlHandler.HH_LIST_RESUME.name(), null));
        processor.processLinks();

        verify(rawDataStorageDao, times(1)).saveHhResume(anyObject(),
                eq("{\"dateCreate\":\"2018-01-01 00:00:00\",\"area\":\"\",\"aa\":\"aa\"}"));
        Assert.assertNull(dao.getDownloadableLink());
    }


}
