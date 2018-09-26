package ru.statjobs.loader.common.dao;


import ru.statjobs.loader.common.dto.DownloadableLink;

import java.util.List;

public interface DownloadableLinkDao {

    boolean createDownloadableLink(DownloadableLink link);

    boolean createDownloadableLinks(List<DownloadableLink> links);

    boolean deleteDownloadableLink(DownloadableLink link);

    DownloadableLink getDownloadableLink();
}
