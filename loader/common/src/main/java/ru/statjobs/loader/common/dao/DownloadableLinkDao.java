package ru.statjobs.loader.common.dao;


import ru.statjobs.loader.common.dto.DownloadableLink;

public interface DownloadableLinkDao {

    boolean createDownloadableLink(DownloadableLink link);

    boolean deleteDownloadableLink(DownloadableLink link);

    DownloadableLink getDownloadableLink();
}
