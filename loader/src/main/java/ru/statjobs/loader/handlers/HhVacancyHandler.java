package ru.statjobs.loader.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.dao.DownloadableLinkDao;
import ru.statjobs.loader.dao.RawDataStorageDao;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.utils.Downloader;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class HhVacancyHandler implements LinkHandler{

    private static final Logger LOGGER = LoggerFactory.getLogger(HhVacancyHandler.class);

    public static final int DOWNLOAD_TIMEOUT = 5000;
    private final Downloader downloader;
    private final RawDataStorageDao rawDataStorageDao;
    private final DownloadableLinkDao downloadableLinkDao;

    public HhVacancyHandler(
            Downloader downloader,
            RawDataStorageDao rawDataStorageDao,
            DownloadableLinkDao downloadableLinkDao
    ) {
        this.downloader = downloader;
        this.rawDataStorageDao = rawDataStorageDao;
        this.downloadableLinkDao = downloadableLinkDao;
    }

    @Override
    public void process(DownloadableLink link) {
        LOGGER.debug("load vacancy url: {}", link.getUrl());
        Downloader.DownloaderResult downloaderResult = downloader.download(link.getUrl(), StandardCharsets.UTF_8, DOWNLOAD_TIMEOUT);
        int responseCode = downloaderResult.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            rawDataStorageDao.saveHhVacancy(link, downloaderResult.getText());
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            LOGGER.debug("vacancy not found. url: {}", link.getUrl());
        } else {
            throw new RuntimeException("fail load url: " + link.getUrl() + ". error code: " + responseCode);
        }
        downloadableLinkDao.deleteDownloadableLink(link);
    }
}
