package ru.statjobs.loader.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.Consts;
import ru.statjobs.loader.common.dao.DownloadableLinkDao;
import ru.statjobs.loader.common.dao.RawDataStorageDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.utils.Downloader;
import ru.statjobs.loader.utils.JsonUtils;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HhVacancyHandler implements LinkHandler{

    private static final Logger LOGGER = LoggerFactory.getLogger(HhVacancyHandler.class);

    public static final int DOWNLOAD_TIMEOUT = 5000;
    private final Downloader downloader;
    private final RawDataStorageDao rawDataStorageDao;
    private final DownloadableLinkDao downloadableLinkDao;
    private final JsonUtils jsonUtils;

    public HhVacancyHandler(
            Downloader downloader,
            RawDataStorageDao rawDataStorageDao,
            DownloadableLinkDao downloadableLinkDao,
            JsonUtils jsonUtils
    ) {
        this.downloader = downloader;
        this.rawDataStorageDao = rawDataStorageDao;
        this.downloadableLinkDao = downloadableLinkDao;
        this.jsonUtils = jsonUtils;
    }

    @Override
    public void process(DownloadableLink link) {
        LOGGER.debug("load vacancy url: {}", link.getUrl());
        Downloader.DownloaderResult downloaderResult = downloader.download(link.getUrl(), StandardCharsets.UTF_8, DOWNLOAD_TIMEOUT);
        int responseCode = downloaderResult.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String json = downloaderResult.getText();
            if (json.length() > Consts.MAX_RAW_JSON_LENGTH) {
                json = reduceMessageLength(json);
            }
            rawDataStorageDao.saveHhVacancy(link, json);
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            LOGGER.debug("vacancy not found. url: {}", link.getUrl());
        } else {
            throw new RuntimeException("fail load url: " + link.getUrl() + ". error code: " + responseCode);
        }
        downloadableLinkDao.deleteDownloadableLink(link);
    }

    String reduceMessageLength(String str) {
        Map<String, Object> map = jsonUtils.readString(str);
        map.put("branded_description", "DEL");
        return jsonUtils.createString(map);
    }
}
