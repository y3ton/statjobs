package ru.statjobs.loader.dao;

public interface RawDataStorageDao {

    void saveHhVacancy(String url, String json);

}
