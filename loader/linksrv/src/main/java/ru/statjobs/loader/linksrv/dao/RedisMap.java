package ru.statjobs.loader.linksrv.dao;

public interface RedisMap {

    String get(String key);

    Long del(String key);

    String set(String key, String value);

    boolean setIfNotExists(String key, String value);

}
