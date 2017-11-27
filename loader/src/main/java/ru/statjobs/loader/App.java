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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {

    public static final Integer PER_PAGE = 100;
    public static final Integer SEARCH_PERIOD = 1;

    private Downloader downloader;
    private UrlConstructor urlConstructor;
    private JsonUtils jsonUtils;
    private QueueDownloadableLinkDao queueDownloadableLinkDao;
    private RawDataStorageDao rawDataStorageDao;
    private HhDictionaryDao hhDictionaryDao;
    private List<HhSpecialization> specialization;
    private Map<String, String> cities;
    private Map<String, String> experience;

    private void init(Connection connection) {
        downloader = new Downloader();
        urlConstructor = new UrlConstructor();
        jsonUtils = new JsonUtils();
        queueDownloadableLinkDao = new QueueDownloadableLinkDaoStub();
        rawDataStorageDao = new RawDataStorageDaoImpl(connection);
        hhDictionaryDao = new HhDictionaryDaoImpl(jsonUtils);
        specialization = hhDictionaryDao.getSpecialization().stream()
                .filter(spec-> "Информационные технологии, интернет, телеком".equals(spec.getSpecializationGroup()))
                .limit(10)
                .collect(Collectors.toList());
        cities = hhDictionaryDao.getCity();
        experience = hhDictionaryDao.getExperience();
    }

    private void process() {
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/statjobs")) {
            init(connection);
            List<DownloadableLink> firstLink = initHhLink(cities, specialization, experience, urlConstructor);
            firstLink.forEach(queueDownloadableLinkDao::createDownloadableLink);
            Map<UrlHandler, LinkHandler> locatorHandlers = createUrlHandlerLocator();
            processLink(queueDownloadableLinkDao, locatorHandlers);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        new App().process();
    }

    private void processLink(QueueDownloadableLinkDao queueDownloadableLinkDao, Map<UrlHandler, LinkHandler> mapHandler) {
        DownloadableLink link;
        while ((link = queueDownloadableLinkDao.getDownloadableLink()) != null) {
            System.out.println(link.getUrl());
            mapHandler.get(UrlHandler.valueOf(link.getHandlerName()))
                    .process(link.getUrl());
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
            UrlConstructor urlConstructor
    ) {
        return cities.values().stream()
                .map(city ->  specialization.stream()
                        .map(spec -> experience.values().stream()
                                .map(exp -> urlConstructor.createHhVacancyUrl(spec.getCode(), SEARCH_PERIOD, city, exp, 0, PER_PAGE))
                                .map(url -> new DownloadableLink(url, UrlHandler.HH_LIST_VACANCIES.name()))
                        )
                        .flatMap(l -> l))
                .flatMap(l -> l)
                .collect(Collectors.toList());
    }

}
