package ru.statjobs.loader.linksrv.redismock;

import ru.statjobs.loader.linksrv.dao.RedisQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedisQueueMock implements RedisQueue {

    public Map<String, List<String>> map = new HashMap<>();


    @Override
    public void push(String queue, String value) {
        List<String> list = map.get(queue);
        if (list == null) {
            list = new ArrayList<>();
            map.put(queue, list);
        }
        list.add(value);

    }

    @Override
    public String pop(String queue) {
        List<String> list = map.get(queue);
        list = map.get(queue);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.remove(list.size() - 1);
    }
}
