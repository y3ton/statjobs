package ru.statjobs.loader.linksrv.dao;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.Closeable;

@Service
public class RedisDao implements RedisMap, RedisQueue, Closeable {

    final JedisPool pool;

    @Override
    public void close() {
        pool.close();
    }

    public RedisDao(String host, int port) {
        this.pool = new JedisPool(new JedisPoolConfig(), host, port);
    }

    @Override
    public String get(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.get(key);
        }
    }

    @Override
    public String set(String key, String value) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.set(key, value);
        }
    }

    @Override
    public boolean setIfNotExists(String key, String value) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.setnx(key, value) != 0;
        }
    }

    @Override
    public void push(String queue, String value) {
        try (Jedis jedis = pool.getResource()) {
            jedis.lpush(queue, value);
        }
    }

    @Override
    public String pop(String queue) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.lpop(queue);
        }
    }
}
