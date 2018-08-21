package ru.statjobs.loader.handlers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.common.dao.DownloadableLinkDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.url.UrlTypes;
import ru.statjobs.loader.url.UrlConstructor;
import ru.statjobs.loader.utils.Downloader;
import ru.statjobs.loader.utils.JsonUtils;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HhListVacanciesHandler implements  LinkHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HhListVacanciesHandler.class);

    public static final int DOWNLOAD_TIMEOUT = 5000;

    private final Downloader downloader;
    private final JsonUtils jsonUtils;
    private final DownloadableLinkDao downloadableLinkDao;
    private final UrlConstructor urlConstructor;

    public HhListVacanciesHandler(
            Downloader downloader,
            JsonUtils jsonUtils,
            DownloadableLinkDao downloadableLinkDao,
            UrlConstructor urlConstructor
    ) {
        this.downloader = downloader;
        this.jsonUtils = jsonUtils;
        this.downloadableLinkDao = downloadableLinkDao;
        this.urlConstructor = urlConstructor;
    }

    @Override
    public void process(DownloadableLink link) {
        LOGGER.debug("load vacancies list url {}", link.getUrl());
        Downloader.DownloaderResult downloaderResult = downloader.download(link.getUrl(), StandardCharsets.UTF_8, DOWNLOAD_TIMEOUT);
        int responseCode = downloaderResult.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("fail load url " + link.getUrl() + ". error code: " + responseCode);
        }
        HashMap<String, Object> map = jsonUtils.readString(downloaderResult.getText());
        List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("items");
        // hh page has no data
        if (items.size() != 0) {
            items.stream()
                    .map(item -> (String) item.get("url"))
                    .filter(StringUtils::isNotBlank)
                    .map(urlVacancy -> new DownloadableLink(urlVacancy, link.getSequenceNum(), UrlTypes.HH_VACANCY, null))
                    .forEach(downloadableLinkDao::createDownloadableLink);

            DownloadableLink nextLink = new DownloadableLink(
                    urlConstructor.hhUrlNextPage(link.getUrl()),
                    link.getSequenceNum(),
                    UrlTypes.HH_LIST_VACANCIES,
                    null
            );
            downloadableLinkDao.createDownloadableLink(nextLink);
        }
        downloadableLinkDao.deleteDownloadableLink(link);
    }
}
