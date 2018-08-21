package ru.statjobs.loader.loadsrv;

import ru.statjobs.loader.common.dao.RawDataStorageDao;
import ru.statjobs.loader.common.dto.RawData;
import ru.statjobs.loader.common.url.UrlTypes;

public class RawDataStorageDaoProxy {

    final RawDataStorageDao storageDao;

    public RawDataStorageDaoProxy(RawDataStorageDao storageDao) {
        this.storageDao = storageDao;
    }

    public void save(RawData rawData) {
        UrlTypes urlType = rawData.getLink().getHandlerName();
        if (urlType == UrlTypes.HH_VACANCY) {
            storageDao.saveHhVacancy(rawData.getLink(), rawData.getJson());
        }else if (urlType == UrlTypes.HH_RESUME) {
            storageDao.saveHhResume(rawData.getLink(), rawData.getJson());
        } else {
            throw new RuntimeException("incompatible url type " + (urlType == null ? "null" : urlType.name()));
        }
    }


}
