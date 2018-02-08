package ru.statjobs.loader.dao;

import ru.statjobs.loader.dto.DownloadableLink;

public interface RawDataStorageDao {

    void saveHhVacancy(DownloadableLink link, String json);

    void saveHhResume(DownloadableLink link, String json);

}
