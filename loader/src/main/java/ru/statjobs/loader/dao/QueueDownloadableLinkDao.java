package ru.statjobs.loader.dao;

import ru.statjobs.loader.dto.DownloadableLink;

public interface QueueDownloadableLinkDao {

    boolean createDownloadableLink(DownloadableLink downloadableLink);

    boolean deleteDownloadableLink(String url);

    DownloadableLink getDownloadableLink();
}
