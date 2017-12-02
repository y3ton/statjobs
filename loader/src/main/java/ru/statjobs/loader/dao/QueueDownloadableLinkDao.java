package ru.statjobs.loader.dao;

import ru.statjobs.loader.dto.DownloadableLink;

public interface QueueDownloadableLinkDao {

    boolean createDownloadableLink(DownloadableLink link);

    boolean deleteDownloadableLink(DownloadableLink link);

    DownloadableLink getDownloadableLink();
}
