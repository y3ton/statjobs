package ru.statjobs.loader.app;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.Const;
import ru.statjobs.loader.JsScript;
import ru.statjobs.loader.LinkProcessor;
import ru.statjobs.loader.SeleniumBrowser;
import ru.statjobs.loader.dao.DownloadableLinkDao;
import ru.statjobs.loader.dao.DownloadableLinkDaoPostgresImpl;
import ru.statjobs.loader.dao.RawDataStorageDaoPostgresImpl;
import ru.statjobs.loader.url.UrlConstructor;
import ru.statjobs.loader.utils.Downloader;
import ru.statjobs.loader.utils.FileUtils;
import ru.statjobs.loader.utils.JsonUtils;
import ru.statjobs.loader.utils.PropertiesUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class HandlerApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerApp.class);

    private SeleniumBrowser seleniumBrowser;
    private LinkProcessor linkProcessor;
    private DownloadableLinkDao downloadableLinkDao;


    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        HandlerApp handlerApp = new HandlerApp();
        Properties props = new PropertiesUtils().loadProperties(Const.PROPERTIES_FILE);
        for (int i = 0; i < Const.HANDLER_RESTART_ATTEMPT; i++) {
            try {
                handlerApp.process(props);
            } catch(Exception ex) {
                LOGGER.error("Process HandlerApp is fail", ex);
            } finally {
                try {
                    handlerApp.close();
                } catch (Exception closeException) {
                    LOGGER.error("Selenium close fail", closeException);
                }
            }
            LOGGER.info("Attempt {} finished, timeout {}", i, Const.HANDLER_RESTART_TIMEOUT);
            Thread.sleep(Const.HANDLER_RESTART_TIMEOUT);
        }
    }

    private void init(Properties properties, Connection connection) {
        JsonUtils jsonUtils = new JsonUtils();

        downloadableLinkDao = new DownloadableLinkDaoPostgresImpl(connection, jsonUtils);
        seleniumBrowser = new SeleniumBrowser(
                properties.getProperty("webdriverpath"),
                Boolean.valueOf(properties.getProperty("headless")),
                true);

        linkProcessor = new LinkProcessor(
                new Downloader(),
                new UrlConstructor(),
                new JsonUtils(),
                downloadableLinkDao,
                new RawDataStorageDaoPostgresImpl(connection),
                seleniumBrowser,
                new JsScript(new FileUtils())
        );
    }

    private void close() {
        seleniumBrowser.close();
    }

    private void process(Properties properties) {
        LOGGER.info("connect to DB. url: {}", properties.getProperty("url"));
        try (Connection connection = DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("user"),
                properties.getProperty("password")))
        {
            init(properties, connection);
            linkProcessor.processLinks();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
