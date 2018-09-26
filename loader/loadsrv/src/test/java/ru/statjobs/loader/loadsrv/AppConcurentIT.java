package ru.statjobs.loader.loadsrv;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.jndi.JndiContext;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.statjobs.loader.Consts;
import ru.statjobs.loader.common.dto.RawData;
import ru.statjobs.loader.dao.HttpUtils;
import ru.statjobs.loader.dao.RawDataStorageDaoHttpImpl;
import ru.statjobs.loader.utils.JsonUtils;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AppConcurentIT {

    static final JsonUtils jsonUtils = new JsonUtils();
    static final HttpUtils httpUtils = new HttpUtils();
    static final String endpointUrl = "http://127.0.0.1:"+  Consts.ENDPOINT_PORT +  Consts.ENDPOINT_URL;

    RawDataStorageDaoHttpImpl daoHttp = new RawDataStorageDaoHttpImpl(
            httpUtils,
            jsonUtils,
            endpointUrl,
            "key"
    );

    JndiContext jndiContext;
    CamelContext camelContext;

    @Before
    public void before() throws Exception {
        jndiContext = new JndiContext();
        camelContext = new DefaultCamelContext(jndiContext);
    }

    @After
    public void after() throws Exception {
        camelContext.stop();
        jndiContext.close();
        daoHttp.stop();
    }

    @Test
    public void routeHttpTest() throws Exception {
        AtomicInteger a = new AtomicInteger(0);
        Map<String, String> mapThread = new ConcurrentHashMap();
        RawDataStorageDaoProxy postgresDao = new RawDataStorageDaoProxy(null) {
            @Override
            public void save(RawData rawData) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                mapThread.put(Thread.currentThread().getName(), "");
                a.incrementAndGet();
            }
        };

        jndiContext.bind("postgresDao", postgresDao);
        Properties props = new Properties();
        props.setProperty("linksrvkey", "key");
        new App().createRoute(camelContext, props, false);
        camelContext.start();
        Thread.sleep(100);
        String longString = StringUtils.repeat("t", 10000);
        String msg = "{\"link\":{\"url\":\"url123\",\"sequenceNum\":1,\"handlerName\":\"HH_RESUME\",\"props\":null},\"json\":\"" + longString + "\"}";
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient = new HttpClient();
                try {
                    httpClient.start();

                    for (int i = 0; i < 50; i++) {
                        httpClient.POST(endpointUrl).header("Authorization", "key").content(new StringContentProvider(msg), "application/json").send();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        httpClient.stop();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        };

        Thread thread1 = new Thread(runnable);
        Thread thread2 = new Thread(runnable);
        Thread thread3 = new Thread(runnable);
        thread1.start();
        thread2.start();
        thread3.start();
        thread1.join();
        thread2.join();
        thread3.join();

        Assert.assertEquals(150, a.get());
        Assert.assertEquals(1, mapThread.keySet().size());

    }

}
