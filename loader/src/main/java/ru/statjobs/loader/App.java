package ru.statjobs.loader;


import ru.statjobs.loader.dao.*;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.dto.HhDictionary;
import ru.statjobs.loader.handlers.*;
import ru.statjobs.loader.url.InitUrlCreator;
import ru.statjobs.loader.url.UrlConstructor;
import ru.statjobs.loader.url.UrlHandler;
import ru.statjobs.loader.utils.Downloader;
import ru.statjobs.loader.utils.FileUtils;
import ru.statjobs.loader.utils.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {



    private Downloader downloader;
    private UrlConstructor urlConstructor;
    private JsonUtils jsonUtils;
    private QueueDownloadableLinkDao queueDownloadableLinkDao;
    private RawDataStorageDao rawDataStorageDao;
    private HhDictionaryDao hhDictionaryDao;
    private List<HhDictionary> specialization;
    private List<HhDictionary> industries;
    private Map<String, String> cities;
    private Map<String, String> experience;
    private InitUrlCreator initUrlCreator;
    private FileUtils fileUtils;
    private SeleniumBrowser seleniumBrowser;
    private JsScript jsScript;

    public static void main(String[] args) throws IOException, SQLException {
        App app = new App();
        Properties props = app.loadProperties();
        for (int i = 0; i < 10; i++) {
            try {
                app.process(props);
            } finally {
                app.close();
            }
            try {
                Thread.sleep(15 * 60 *1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void init(Properties properties, Connection connection) {

        downloader = new Downloader();
        urlConstructor = new UrlConstructor();
        jsonUtils = new JsonUtils();
        fileUtils = new FileUtils();
        queueDownloadableLinkDao = new QueueDownloadableLinkDaoImpl(connection, jsonUtils);
        rawDataStorageDao = new RawDataStorageDaoImpl(connection);
        hhDictionaryDao = new HhDictionaryDaoImpl(jsonUtils, fileUtils);
        specialization = hhDictionaryDao.getSpecialization();
        cities = hhDictionaryDao.getCity();
        industries = hhDictionaryDao.getIndustries();
        experience = hhDictionaryDao.getExperience();
        initUrlCreator = new InitUrlCreator();
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
            // create base hh url
            int sequenceNum = (int) Instant.now().getEpochSecond();
            List<DownloadableLink> firstLink = new ArrayList<>();
            //firstLink.addAll(initUrlCreator.initHhItVacancyLink(urlConstructor, sequenceNum, Const.HH_PER_PAGE, cities, specialization, experience, industries));
            //firstLink.addAll(initUrlCreator.initHhItResumeLink(urlConstructor, sequenceNum, Const.HH_PER_PAGE, cities, specialization, Const.HH_SEARCH_PERIOD));
            firstLink.forEach(queueDownloadableLinkDao::createDownloadableLink);
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

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = App.class.getClassLoader().getResourceAsStream(Const.PROPERTIES_FILE)){
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
