package ru.statjobs.loader.app;


import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.*;
import ru.statjobs.loader.common.dao.DownloadableLinkDao;
import ru.statjobs.loader.common.dao.RawDataStorageDao;
import ru.statjobs.loader.dao.DownloadableLinkDaoHttpImpl;
import ru.statjobs.loader.dao.HttpUtils;
import ru.statjobs.loader.dao.RawDataStorageDaoJmsImpl;
import ru.statjobs.loader.url.UrlConstructor;
import ru.statjobs.loader.utils.Downloader;
import ru.statjobs.loader.utils.FileUtils;
import ru.statjobs.loader.utils.JsonUtils;
import ru.statjobs.loader.utils.PropertiesUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class HandlerApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerApp.class);

    private SeleniumBrowser seleniumBrowser;
    private LinkProcessor linkProcessor;
    private DownloadableLinkDao downloadableLinkDao;
    private RawDataStorageDao rawDataStorage;

    public static void main(String[] args) throws InterruptedException {
        HandlerApp handlerApp = new HandlerApp();
        Properties props = new PropertiesUtils().loadProperties(Consts.PROPERTIES_FILE);
        for (int i = 0; i < ClientConsts.HANDLER_RESTART_ATTEMPT; i++) {
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
            LOGGER.info("Attempt {} finished, timeout {}", i, ClientConsts.HANDLER_RESTART_TIMEOUT);
            Thread.sleep(ClientConsts.HANDLER_RESTART_TIMEOUT);
        }
    }

    private void init(Properties properties) throws SQLException {
        JsonUtils jsonUtils = new JsonUtils();
        HttpUtils httpUtils = new HttpUtils();

        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard()
                        .withRegion(Regions.US_WEST_2)
                        .withCredentials(new PropertiesFileCredentialsProvider(properties.getProperty("awscredfile"))));

        rawDataStorage = new RawDataStorageDaoJmsImpl(connectionFactory, Consts.RAW_QUEUE_NAME,  jsonUtils);
        downloadableLinkDao = new DownloadableLinkDaoHttpImpl(
                httpUtils,
                jsonUtils,
                properties.getProperty("linksrv"),
                properties.getProperty("linksrvkey")
        );
        ((DownloadableLinkDaoHttpImpl)downloadableLinkDao).start();

        seleniumBrowser = new SeleniumBrowser(
                properties.getProperty("webdriverpath"),
                Boolean.valueOf(properties.getProperty("headless")),
                true);

        linkProcessor = new LinkProcessor(
                new Downloader(),
                new UrlConstructor(),
                new JsonUtils(),
                downloadableLinkDao,
                rawDataStorage,
                seleniumBrowser,
                new JsScript(new FileUtils())
        );
    }

    private void close() {
        if (downloadableLinkDao != null) {
            ((DownloadableLinkDaoHttpImpl) downloadableLinkDao).stop();
        }
        if (rawDataStorage != null) {
            ((RawDataStorageDaoJmsImpl) rawDataStorage).close();
        }
        if (seleniumBrowser != null) {
            seleniumBrowser.close();
        }
    }

    private void process(Properties properties) {
        LOGGER.info("connect to DB. url: {}", properties.getProperty("url"));
        try
        {
            init(properties);
            linkProcessor.processLinks();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
