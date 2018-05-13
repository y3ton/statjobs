package ru.statjobs.loader.app;


import ru.statjobs.loader.Const;
import ru.statjobs.loader.JsScript;
import ru.statjobs.loader.SeleniumBrowser;
import ru.statjobs.loader.dao.QueueDownloadableLinkDao;
import ru.statjobs.loader.dao.QueueDownloadableLinkDaoImpl;
import ru.statjobs.loader.dao.RawDataStorageDao;
import ru.statjobs.loader.dao.RawDataStorageDaoImpl;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.handlers.*;
import ru.statjobs.loader.url.UrlConstructor;
import ru.statjobs.loader.url.UrlHandler;
import ru.statjobs.loader.utils.Downloader;
import ru.statjobs.loader.utils.FileUtils;
import ru.statjobs.loader.utils.JsonUtils;
import ru.statjobs.loader.utils.PropertiesUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HandlerApp {

    private Downloader downloader;
    private UrlConstructor urlConstructor;
    private JsonUtils jsonUtils;
    private QueueDownloadableLinkDao queueDownloadableLinkDao;
    private RawDataStorageDao rawDataStorageDao;
    private FileUtils fileUtils;
    private SeleniumBrowser seleniumBrowser;
    private JsScript jsScript;

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        HandlerApp handlerApp = new HandlerApp();
        Properties props = new PropertiesUtils().loadProperties(Const.PROPERTIES_FILE);
        for (int i = 0; i < Const.HANDLER_RESTART_ATTEMPT; i++) {
            try {
                handlerApp.process(props);
            } finally {
                handlerApp.close();
            }
            Thread.sleep(Const.HANDLER_RESTART_TIMEOUT);
        }
    }

    private void init(Properties properties, Connection connection) {
        downloader = new Downloader();
        urlConstructor = new UrlConstructor();
        jsonUtils = new JsonUtils();
        fileUtils = new FileUtils();
        queueDownloadableLinkDao = new QueueDownloadableLinkDaoImpl(connection, jsonUtils);
        rawDataStorageDao = new RawDataStorageDaoImpl(connection);
        seleniumBrowser = new SeleniumBrowser(properties.getProperty("webdriverpath"));
        jsScript = new JsScript(fileUtils);
    }

    private void close() {
        seleniumBrowser.close();
    }

    private void process(Properties properties) {
        try (Connection connection = DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("user"),
                properties.getProperty("password")))
        {
            init(properties, connection);
            Map<UrlHandler, LinkHandler> locatorHandlers = createUrlHandlerLocator();
            processLink(queueDownloadableLinkDao, locatorHandlers);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void processLink(QueueDownloadableLinkDao queueDownloadableLinkDao, Map<UrlHandler, LinkHandler> mapHandler) {
        DownloadableLink link;
        while ((link = queueDownloadableLinkDao.getDownloadableLink()) != null) {
            System.out.println(link.getUrl());
            mapHandler.get(UrlHandler.valueOf(link.getHandlerName()))
                    .process(link);
        }
    }

    private Map<UrlHandler, LinkHandler> createUrlHandlerLocator() {
        return Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>(
                        UrlHandler.HH_RESUME,
                        new HhResumeHandler(seleniumBrowser, jsScript, rawDataStorageDao, queueDownloadableLinkDao)),
                new AbstractMap.SimpleEntry<>(
                        UrlHandler.HH_LIST_RESUME,
                        new HhListResumeHandler(seleniumBrowser, jsScript, queueDownloadableLinkDao, urlConstructor)),
                new AbstractMap.SimpleEntry<>(
                        UrlHandler.HH_LIST_VACANCIES,
                        new HhListVacanciesHandler(downloader, jsonUtils, queueDownloadableLinkDao, urlConstructor)),
                new AbstractMap.SimpleEntry<>(
                        UrlHandler.HH_VACANCY,
                        new HhVacancyHandler(downloader, rawDataStorageDao, queueDownloadableLinkDao)))
                .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));
    }

}
