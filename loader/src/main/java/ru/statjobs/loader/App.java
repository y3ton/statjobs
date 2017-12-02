package ru.statjobs.loader;


import ru.statjobs.loader.dao.*;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.dto.HhSpecialization;
import ru.statjobs.loader.handlers.HhListVacanciesHandler;
import ru.statjobs.loader.handlers.HhVacancyHandler;
import ru.statjobs.loader.handlers.LinkHandler;
import ru.statjobs.loader.utils.Downloader;
import ru.statjobs.loader.utils.JsonUtils;

import java.io.*;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {

    public static final Integer PER_PAGE = 100;
    public static final Integer SEARCH_PERIOD = 1;
    public static final String PROPERTIES_FILE = "app.properties";

    private Downloader downloader;
    private UrlConstructor urlConstructor;
    private JsonUtils jsonUtils;
    private QueueDownloadableLinkDao queueDownloadableLinkDao;
    private RawDataStorageDao rawDataStorageDao;
    private HhDictionaryDao hhDictionaryDao;
    private List<HhSpecialization> specialization;
    private Map<String, String> cities;
    private Map<String, String> experience;

    public static void main(String[] args) throws IOException, SQLException {
        new App().process();
    }

    private void init(Connection connection) {
        downloader = new Downloader();
        urlConstructor = new UrlConstructor();
        jsonUtils = new JsonUtils();
        //queueDownloadableLinkDao = new QueueDownloadableLinkDaoStub();
        queueDownloadableLinkDao = new QueueDownloadableLinkDaoImpl(connection);
        rawDataStorageDao = new RawDataStorageDaoImpl(connection);
        hhDictionaryDao = new HhDictionaryDaoImpl(jsonUtils);
        specialization = hhDictionaryDao.getSpecialization().stream()
                .filter(spec-> "Информационные технологии, интернет, телеком".equals(spec.getSpecializationGroup()))
                .filter(spec-> "Программирование, Разработка".equals(spec.getSpecialization()))
                .collect(Collectors.toList());
        cities = hhDictionaryDao.getCity().entrySet().stream()
                .filter(map -> map.getKey().equals("Москва"))
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        experience = hhDictionaryDao.getExperience();

    }

    private void process() {
        Properties properties = loadProperties();
        try (Connection connection = DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("user"),
                properties.getProperty("password")))
        {
            init(connection);
            // create base hh url
            int sequenceNum = (int) Instant.now().getEpochSecond();
            List<DownloadableLink> firstLink = initHhLink(cities, specialization, experience, urlConstructor, sequenceNum);
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
                        UrlHandler.HH_LIST_VACANCIES,
                        new HhListVacanciesHandler(downloader, jsonUtils, queueDownloadableLinkDao, urlConstructor)),
                new AbstractMap.SimpleEntry<>(
                        UrlHandler.HH_VACANCY,
                        new HhVacancyHandler(downloader, rawDataStorageDao, queueDownloadableLinkDao)))
                .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));
    }


    private List<DownloadableLink> initHhLink(
            Map<String, String> cities,
            List<HhSpecialization> specialization,
            Map<String, String> experience,
            UrlConstructor urlConstructor,
            int sequenceNum
    ) {
        return cities.values().stream()
                .map(city ->  specialization.stream()
                        .map(spec -> experience.values().stream()
                                .map(exp -> urlConstructor.createHhVacancyUrl(spec.getCode(), SEARCH_PERIOD, city, exp, 0, PER_PAGE))
                                .map(url -> new DownloadableLink(url, sequenceNum, UrlHandler.HH_LIST_VACANCIES.name()))
                        )
                        .flatMap(l -> l))
                .flatMap(l -> l)
                .collect(Collectors.toList());
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = App.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)){
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
