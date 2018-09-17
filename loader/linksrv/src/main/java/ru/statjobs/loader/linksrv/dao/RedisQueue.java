package ru.statjobs.loader.linksrv.dao;

public interface RedisQueue {

    void push(String queue, String value);

    String pop(String queue);

}
