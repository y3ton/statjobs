package ru.statjobs.loader.loadsrv;

import org.junit.Test;
import ru.statjobs.loader.common.dao.RawDataStorageDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.dto.RawData;
import ru.statjobs.loader.common.url.UrlTypes;

import static  org.mockito.Mockito.*;

public class RawDataStorageDaoProxyTest {

    @Test
    public void saveVacancyTest() {
        RawDataStorageDao dao = mock(RawDataStorageDao.class);
        RawDataStorageDaoProxy proxy = new RawDataStorageDaoProxy(dao);
        RawData rawData = new RawData(new DownloadableLink("url", 1, UrlTypes.HH_VACANCY, null), "");
        proxy.save(rawData);
        verify(dao, times(1)).saveHhVacancy(anyObject(), anyString());
        verify(dao, times(0)).saveHhResume(anyObject(), anyString());
    }

    @Test
    public void saveResumeTest() {
        RawDataStorageDao dao = mock(RawDataStorageDao.class);
        RawDataStorageDaoProxy proxy = new RawDataStorageDaoProxy(dao);
        RawData rawData = new RawData(new DownloadableLink("url", 1, UrlTypes.HH_RESUME, null), "");
        proxy.save(rawData);
        verify(dao, times(0)).saveHhVacancy(anyObject(), anyString());
        verify(dao, times(1)).saveHhResume(anyObject(), anyString());
    }

    @Test(expected = RuntimeException.class)
    public void saveExceptionTest() {
        RawDataStorageDao dao = mock(RawDataStorageDao.class);
        RawDataStorageDaoProxy proxy = new RawDataStorageDaoProxy(dao);
        RawData rawData = new RawData(new DownloadableLink("url", 1, UrlTypes.HH_LIST_RESUME, null), "");
        proxy.save(rawData);
    }
}
