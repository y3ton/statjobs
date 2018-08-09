package ru.statjobs.loader.common.dao;


import ru.statjobs.loader.common.dto.DownloadableLink;

public interface RawDataStorageDao {

    void saveHhVacancy(DownloadableLink link, String json);

    void saveHhResume(DownloadableLink link, String json);

}
