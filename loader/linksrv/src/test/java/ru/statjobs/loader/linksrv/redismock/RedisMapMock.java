package ru.statjobs.loader.linksrv.redismock;

import ru.statjobs.loader.linksrv.dao.RedisMap;

import java.util.HashMap;
import java.util.Map;

public class RedisMapMock implements RedisMap {

    public Map<String, String> map = new HashMap<>();

    @Override
    public String get(String key) {
        return map.get(key);
    }

    @Override
    public Long del(String key) {
        return map.remove(key) != null ? 1L: 0L;
    }

    @Override
    public String set(String key, String value) {
        return map.put(key, value);
    }

    @Override
    public boolean setIfNotExists(String key, String value) {
        if (map.containsKey(key)) {
            return false;
        } else {
            map.put(key, value);
            return true;
        }
    }
}
