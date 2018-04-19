package ru.statjobs.loader.handlers;

import ru.statjobs.loader.dao.QueueDownloadableLinkDao;
import ru.statjobs.loader.dao.RawDataStorageDao;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.utils.Downloader;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class HhVacancyHandler implements LinkHandler{

    public static final int DOWNLOAD_TIMEOUT = 5000;
    private final Downloader downloader;
    private final RawDataStorageDao rawDataStorageDao;
    private final QueueDownloadableLinkDao queueDownloadableLinkDao;

    public HhVacancyHandler(
            Downloader downloader,
            RawDataStorageDao rawDataStorageDao,
            QueueDownloadableLinkDao queueDownloadableLinkDao
    ) {
        this.downloader = downloader;
        this.rawDataStorageDao = rawDataStorageDao;
        this.queueDownloadableLinkDao = queueDownloadableLinkDao;
    }

    @Override
    public void process(DownloadableLink link) {
        Downloader.DownloaderResult downloaderResult = downloader.download(link.getUrl(), StandardCharsets.UTF_8, DOWNLOAD_TIMEOUT);
        int responseCode = downloaderResult.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            rawDataStorageDao.saveHhVacancy(link, downloaderResult.getText());
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            // ignore
        } else {
            throw new RuntimeException("fail load url " + link.getUrl() + ". error code: " + responseCode);
        }
        queueDownloadableLinkDao.deleteDownloadableLink(link);
    }
}
