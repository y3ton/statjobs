package ru.statjobs.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.dao.DownloadableLinkDao;
import ru.statjobs.loader.dao.RawDataStorageDao;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.handlers.*;
import ru.statjobs.loader.url.UrlConstructor;
import ru.statjobs.loader.url.UrlHandler;
import ru.statjobs.loader.utils.Downloader;
import ru.statjobs.loader.utils.JsonUtils;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LinkProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkProcessor.class);

    final private Downloader downloader;
    final private UrlConstructor urlConstructor;
    final private JsonUtils jsonUtils;
    final private DownloadableLinkDao queueDownloadableLinkDao;
    final private RawDataStorageDao rawDataStorageDao;
    final private SeleniumBrowser seleniumBrowser;
    final private JsScript jsScript;

    public LinkProcessor(
            Downloader downloader,
            UrlConstructor urlConstructor,
            JsonUtils jsonUtils,
            DownloadableLinkDao queueDownloadableLinkDao,
            RawDataStorageDao rawDataStorageDao,
            SeleniumBrowser seleniumBrowser,
            JsScript jsScript
    ) {
        this.downloader = downloader;
        this.urlConstructor = urlConstructor;
        this.jsonUtils = jsonUtils;
        this.queueDownloadableLinkDao = queueDownloadableLinkDao;
        this.rawDataStorageDao = rawDataStorageDao;
        this.seleniumBrowser = seleniumBrowser;
        this.jsScript = jsScript;
    }

    public void processLink(DownloadableLinkDao queueDownloadableLinkDao) {
        Map<UrlHandler, LinkHandler> mapHandler = createUrlHandlerLocator();
        DownloadableLink link;
        while ((link = queueDownloadableLinkDao.getDownloadableLink()) != null) {
            LOGGER.info("process url: {}", link.getUrl());
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
