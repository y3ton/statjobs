package ru.statjobs.loader.handlers;

import org.apache.commons.lang3.StringUtils;
import ru.statjobs.loader.UrlConstructor;
import ru.statjobs.loader.UrlHandler;
import ru.statjobs.loader.dao.QueueDownloadableLinkDao;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.utils.Downloader;
import ru.statjobs.loader.utils.JsonUtils;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HhListVacanciesHandler implements  LinkHandler {

    public static final int DOWNLOAD_TIMEOUT = 5000;

    private final Downloader downloader;
    private final JsonUtils jsonUtils;
    private final QueueDownloadableLinkDao queueDownloadableLinkDao;
    private final UrlConstructor urlConstructor;

    public HhListVacanciesHandler(
            Downloader downloader,
            JsonUtils jsonUtils,
            QueueDownloadableLinkDao queueDownloadableLinkDao,
            UrlConstructor urlConstructor
    ) {
        this.downloader = downloader;
        this.jsonUtils = jsonUtils;
        this.queueDownloadableLinkDao = queueDownloadableLinkDao;
        this.urlConstructor = urlConstructor;
    }

    @Override
    public void process(String url) {
        Downloader.DownloaderResult downloaderResult = downloader.download(url, StandardCharsets.UTF_8,DOWNLOAD_TIMEOUT);
        int responseCode = downloaderResult.getResponseCode();
        // hh page has no data
        if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
            return;
        }
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("fail load url " + url + ". error code: " + responseCode);
        }
        HashMap<String, Object> map = jsonUtils.readString(downloaderResult.getText());
        List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("items");

        items.stream()
                .map(item -> (String)item.get("url"))
                .filter(StringUtils::isNotBlank)
                .map(urlVacancy -> new DownloadableLink(urlVacancy, UrlHandler.HH_VACANCY.name()))
                .forEach(queueDownloadableLinkDao::createDownloadableLink);

        DownloadableLink nextLink = new DownloadableLink(urlConstructor.hhVacancyUrlNextPage(url), UrlHandler.HH_VACANCY.name());
        queueDownloadableLinkDao.createDownloadableLink(nextLink);
        queueDownloadableLinkDao.deleteDownloadableLink(url);
    }
}
