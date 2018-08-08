package ru.statjobs.loader.handlers;

import org.junit.*;
import ru.statjobs.loader.Const;
import ru.statjobs.loader.JsScript;
import ru.statjobs.loader.SeleniumBrowser;
import ru.statjobs.loader.dao.DownloadableLinkDao;
import ru.statjobs.loader.dao.DownloadableLinkDaoPostgresImpl;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.testutils.H2Utils;
import ru.statjobs.loader.url.UrlConstructor;
import ru.statjobs.loader.url.UrlHandler;
import ru.statjobs.loader.utils.JsonUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

public class HhListResumeHandlerIT {

    private static Connection connection;

    private static DownloadableLinkDao dao;
    private static SeleniumBrowser browser;
    private static JsScript jsScript;
    private static UrlConstructor urlConstructor;

    @BeforeClass
    public static void start() throws SQLException {
        connection = DriverManager.getConnection ("jdbc:h2:mem:test;MODE=PostgreSQL");
        jsScript = mock(JsScript.class);
        H2Utils.runScript("sql/queue.sql", connection);
        dao = spy(new DownloadableLinkDaoPostgresImpl(connection,  new JsonUtils(), false));
        urlConstructor = new UrlConstructor();
    }

    @Before
    public void startMethod() {
        browser = mock(SeleniumBrowser.class);
    }

    @AfterClass
    public static void stop() throws SQLException {
        connection.close();
    }

    @Test
    public void processTest() {
        HhListResumeHandler handler = new HhListResumeHandler(browser, jsScript, dao, urlConstructor);
        doReturn(false).when(browser).isStart();
        List<List<String>> listUrls = Arrays.asList(
                Arrays.asList("url1", "Обновлено 10 сентября, 11:11"),
                Arrays.asList("url2", "Обновлено 10 января, 00:00"),
                Arrays.asList("url3", "Обновлено 10 декабря, 13:59"));

        doReturn(listUrls).when(browser).execJs(anyString());
        doReturn(3L).when(browser).execJs(eq("return document.querySelectorAll('[data-qa=\"pager-next\"]').length"));

        dao.createDownloadableLink(new DownloadableLink("url", 1, UrlHandler.HH_LIST_RESUME.name(), null));
        DownloadableLink initLink = dao.getDownloadableLink();

        handler.process(initLink);
        verify(browser, times(1)).get("url");
        verify(browser, times(1)).start();

        List<DownloadableLink> list = new ArrayList<>();
        list.add(dao.getDownloadableLink());
        list.add(dao.getDownloadableLink());
        list.add(dao.getDownloadableLink());
        list.add(dao.getDownloadableLink());
        Assert.assertNull(dao.getDownloadableLink());

        list = list.stream()
                .filter(l -> !(
                        l.getUrl().equals("url1") && l.getProps().get(Const.DATE_CREATE_RESUME).endsWith("-09-10 11:11:00") ||
                                l.getUrl().equals("url2") && l.getProps().get(Const.DATE_CREATE_RESUME).endsWith("-01-10 00:00:00") ||
                                l.getUrl().equals("url3") && l.getProps().get(Const.DATE_CREATE_RESUME).endsWith("-12-10 13:59:00") ||
                                l.getUrl().equals("url&page=1") && l.getHandlerName().equals(UrlHandler.HH_LIST_RESUME.name())
                ))
                .collect(Collectors.toList());
        Assert.assertTrue(list.isEmpty());
        Assert.assertNull(dao.getDownloadableLink());
    }

    @Test
    public void processEmptyTest() {
        HhListResumeHandler handler = new HhListResumeHandler(browser, jsScript, dao, urlConstructor);
        doReturn(false).when(browser).isStart();
        List<List<String>> listUrls = Arrays.asList();

        doReturn(listUrls).when(browser).execJs(anyString());
        doReturn(0L).when(browser).execJs(eq("return document.querySelectorAll('[data-qa=\"pager-next\"]').length"));

        handler.process(new DownloadableLink("url", 1, UrlHandler.HH_LIST_RESUME.name(), null));
        verify(browser, times(1)).get("url");
        verify(browser, times(1)).start();
        verify(dao, times(1)).deleteDownloadableLink(anyObject());

        Assert.assertNull(dao.getDownloadableLink());
    }


}
