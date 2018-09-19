package ru.statjobs.loader.linksrv.dao;

import ai.grakn.redismock.RedisServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


import java.io.IOException;

public class RedisDaoIT {

    static RedisServer redisServer;

    @BeforeClass
    public static void init() throws IOException {
        redisServer = RedisServer.newRedisServer();
        redisServer.start();

    }

    @AfterClass
    public static void close() {
        redisServer.stop();
    }

    @Test
    public void simpleMapTest() {
        RedisMap redisDao = new RedisDao(redisServer.getHost(), redisServer.getBindPort());
        Assert.assertNull(redisDao.get("key1"));
        redisDao.set("key1", "v1");
        Assert.assertEquals("v1", redisDao.get("key1"));
        redisDao.set("key1", "v2");
        Assert.assertEquals("v2", redisDao.get("key1"));

        Assert.assertFalse(redisDao.setIfNotExists("key1", "vvvvvvvv"));
        Assert.assertEquals("v2", redisDao.get("key1"));

        Assert.assertTrue(redisDao.setIfNotExists("key22222", "vvvvvvvv"));

        Assert.assertEquals("vvvvvvvv", redisDao.get("key22222"));
        Assert.assertEquals("v2", redisDao.get("key1"));

        redisDao.del("key1");
        Assert.assertEquals("vvvvvvvv", redisDao.get("key22222"));
        Assert.assertNull(redisDao.get("key1"));

        redisDao.del("key22222");
        Assert.assertNull(redisDao.get("key22222"));
        Assert.assertNull(redisDao.get("key1"));

        ((RedisDao)redisDao).close();
    }

    @Test
    public void simpleQTest() {
        RedisQueue redisDao = new RedisDao(redisServer.getHost(), redisServer.getBindPort());

        Assert.assertEquals(null, redisDao.pop("q1"));

        redisDao.push("q1", "v1");
        redisDao.push("q1", "v1");
        redisDao.push("q1", "v2");
        redisDao.push("q1", "v2");

        Assert.assertEquals("v2", redisDao.pop("q1"));
        Assert.assertEquals("v2", redisDao.pop("q1"));
        Assert.assertEquals("v1", redisDao.pop("q1"));
        Assert.assertEquals("v1", redisDao.pop("q1"));
        Assert.assertEquals(null, redisDao.pop("q1"));

        ((RedisDao)redisDao).close();
    }

}
