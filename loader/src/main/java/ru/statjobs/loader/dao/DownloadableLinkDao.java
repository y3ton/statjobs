package ru.statjobs.loader.dao;

import ru.statjobs.loader.dto.DownloadableLink;

public interface DownloadableLinkDao {

    boolean createDownloadableLink(DownloadableLink link);

    boolean deleteDownloadableLink(DownloadableLink link);

    DownloadableLink getDownloadableLink();
}
