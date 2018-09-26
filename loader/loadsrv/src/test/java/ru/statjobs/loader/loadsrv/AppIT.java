package ru.statjobs.loader.loadsrv;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.jndi.JndiContext;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.junit.*;
import ru.statjobs.loader.Consts;
import ru.statjobs.loader.common.dao.RawDataStorageDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.url.UrlTypes;
import ru.statjobs.loader.dao.HttpUtils;
import ru.statjobs.loader.dao.RawDataStorageDaoHttpImpl;
import ru.statjobs.loader.dao.RawDataStorageDaoJmsImpl;
import ru.statjobs.loader.utils.JsonUtils;

import java.util.Properties;

import static org.mockito.Mockito.*;

public class AppIT {

    static final JsonUtils jsonUtils = new JsonUtils();
    static final HttpUtils httpUtils = new HttpUtils();
    static final BrokerService broker = new BrokerService();
    static final String endpointUrl = "http://127.0.0.1:"+  Consts.ENDPOINT_PORT +  Consts.ENDPOINT_URL;

    RawDataStorageDaoHttpImpl daoHttp = new RawDataStorageDaoHttpImpl(
            httpUtils,
            jsonUtils,
            endpointUrl,
            "key"
    );

    JndiContext jndiContext;
    CamelContext camelContext;

    @BeforeClass
    public static void start() throws Exception {
        broker.addConnector("tcp://localhost:61616");
        broker.start();
        broker.waitUntilStarted();
    }

    @AfterClass
    public static void stop() throws Exception {
        broker.stop();
        broker.waitUntilStopped();
    }

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
    public void routeJmsTest() throws Exception {

        RawDataStorageDaoProxy postgresDao = mock(RawDataStorageDaoProxy.class);
        jndiContext.bind("postgresDao", postgresDao);
        camelContext.addComponent("jms", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false"));
        new App().createRoute(camelContext, null, true);
        camelContext.start();

        ProducerTemplate template = camelContext.createProducerTemplate();
        template.sendBody("jms:" + Consts.RAW_QUEUE_NAME, "{\"link\":{\"url\":\"url123\",\"sequenceNum\":1,\"handlerName\":\"HH_RESUME\",\"props\":null},\"json\":\"{}\"}");
        template.sendBody("jms:" + Consts.RAW_QUEUE_NAME, "{\"link\":{\"url\":\"url123\",\"sequenceNum\":1,\"handlerName\":\"HH_RESUME\",\"props\":null},\"json\":\"{}\"}");

        Thread.sleep(100);
        verify(postgresDao, times(2)).save(anyObject());
    }

    @Test
    public void routeHttpTest() throws Exception {
        RawDataStorageDaoProxy postgresDao = mock(RawDataStorageDaoProxy.class);
        jndiContext.bind("postgresDao", postgresDao);
        Properties props = new Properties();
        props.setProperty("linksrvkey", "key");
        new App().createRoute(camelContext, props, false);
        camelContext.start();
        String msg = "{\"link\":{\"url\":\"url123\",\"sequenceNum\":1,\"handlerName\":\"HH_RESUME\",\"props\":null},\"json\":\"{}\"}";
        HttpClient httpClient = new HttpClient();
        httpClient.start();
        Thread.sleep(100);
        httpClient.POST(endpointUrl).header("Authorization", "key").content(new StringContentProvider(msg), "application/json").send();
        httpClient.POST(endpointUrl).header("Authorization", "key").content(new StringContentProvider(msg), "application/json").send();
        verify(postgresDao, times(2)).save(anyObject());
        // check Authorization
        httpClient.POST(endpointUrl).content(new StringContentProvider(msg), "application/json").send();
        verify(postgresDao, times(2)).save(anyObject());
    }

    @Test
    public void failUrlTypeTest() throws Exception {
        RawDataStorageDao postgresMock = mock(RawDataStorageDao.class);
        RawDataStorageDaoProxy proxy = spy(new RawDataStorageDaoProxy(postgresMock));
        jndiContext.bind("postgresDao", proxy);
        camelContext.addComponent("jms", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false"));
        new App().createRoute(camelContext, null, true);
        camelContext.start();

        ProducerTemplate template = camelContext.createProducerTemplate();

        ListAppender.clear();

        template.sendBody("jms:" + Consts.RAW_QUEUE_NAME, "{\"link\":{\"url\":\"url123\",\"sequenceNum\":1,\"handlerName\":\"HH_LIST_RESUME\",\"props\":null},\"json\":\"{}\"}");

        Thread.sleep(100);
        verify(postgresMock, times(0)).saveHhResume(anyObject(), anyString());
        verify(postgresMock, times(0)).saveHhVacancy(anyObject(), anyString());
        verify(proxy, times(1)).save(anyObject());

        Assert.assertTrue(StringUtils.join(ListAppender.getLogMap().get("ERROR"), ";").contains("incompatible url type HH_LIST_RESUME"));
    }

    @Test
    public void checkInvalidJsonException() throws Exception {
        RawDataStorageDao postgresMock = mock(RawDataStorageDao.class);
        RawDataStorageDaoProxy proxy = spy(new RawDataStorageDaoProxy(postgresMock));
        jndiContext.bind("postgresDao", proxy);
        camelContext.addComponent("jms", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false"));
        new App().createRoute(camelContext, null, true);
        camelContext.start();

        ProducerTemplate template = camelContext.createProducerTemplate();

        ListAppender.clear();

        String invalidJson =  "{\"link\":{\"url\":\"url123\",\"sequenceNum\":1,\"handlerName\":\"sdfgsdfgsdfgdsfgsdfg\",\"props\":null},\"json\":\"{}\"}";

        template.sendBody("jms:" + Consts.RAW_QUEUE_NAME, invalidJson);

        Thread.sleep(100);
        verify(postgresMock, times(0)).saveHhResume(anyObject(), anyString());
        verify(postgresMock, times(0)).saveHhVacancy(anyObject(), anyString());
        verify(proxy, times(0)).save(anyObject());
        Thread.sleep(100);

        Assert.assertTrue(StringUtils.join(ListAppender.getLogMap().get("ERROR"), ";").contains(invalidJson));
        Assert.assertTrue(StringUtils.join(ListAppender.getLogMap().get("ERROR"), ";").contains("InvalidFormatException"));

    }

    @Test
    public void appWithDaoHttpTest() throws Exception {

        RawDataStorageDao postgresMock = mock(RawDataStorageDao.class);
        RawDataStorageDaoProxy proxy = spy(new RawDataStorageDaoProxy(postgresMock));
        jndiContext.bind("postgresDao", proxy);
        Properties props = new Properties();
        props.setProperty("linksrvkey", "key");
        new App().createRoute(camelContext, props, false);
        camelContext.start();
        daoHttp.start();

        daoHttp.saveHhResume(new DownloadableLink("url1", 0, UrlTypes.HH_RESUME, null), "{\"a1\":\"1\"}");
        daoHttp.saveHhResume(new DownloadableLink("url2", 0, UrlTypes.HH_RESUME, null), "{\"a2\":\"2\"}");
        daoHttp.saveHhVacancy(new DownloadableLink("url3", 0, UrlTypes.HH_VACANCY, null), "{\"a2\":\"3\"}");

        Thread.sleep(100);
        verify(postgresMock, times(1)).saveHhResume(anyObject(), eq("{\"a1\":\"1\"}"));
        verify(postgresMock, times(1)).saveHhResume(anyObject(), eq("{\"a2\":\"2\"}"));
        verify(postgresMock, times(1)).saveHhVacancy(anyObject(), eq("{\"a2\":\"3\"}"));
    }

    @Test
    public void appWithDaoJmsTest() throws Exception {
        RawDataStorageDaoJmsImpl daoJms = new RawDataStorageDaoJmsImpl(
                new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false"),
                Consts.RAW_QUEUE_NAME,
                jsonUtils
        );

        RawDataStorageDao postgresMock = mock(RawDataStorageDao.class);
        RawDataStorageDaoProxy proxy = spy(new RawDataStorageDaoProxy(postgresMock));
        jndiContext.bind("postgresDao", proxy);
        camelContext.addComponent("jms", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false"));
        new App().createRoute(camelContext, null, true);
        camelContext.start();

        daoJms.saveHhResume(new DownloadableLink("url1", 0, UrlTypes.HH_RESUME, null), "{\"a1\":\"1\"}");
        daoJms.saveHhResume(new DownloadableLink("url2", 0, UrlTypes.HH_RESUME, null), "{\"a2\":\"2\"}");
        daoJms.saveHhVacancy(new DownloadableLink("url3", 0, UrlTypes.HH_VACANCY, null), "{\"a2\":\"3\"}");

        Thread.sleep(100);
        verify(postgresMock, times(1)).saveHhResume(anyObject(), eq("{\"a1\":\"1\"}"));
        verify(postgresMock, times(1)).saveHhResume(anyObject(), eq("{\"a2\":\"2\"}"));
        verify(postgresMock, times(1)).saveHhVacancy(anyObject(), eq("{\"a2\":\"3\"}"));
    }

}
