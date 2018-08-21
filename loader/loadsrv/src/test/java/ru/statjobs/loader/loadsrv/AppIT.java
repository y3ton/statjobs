package ru.statjobs.loader.loadsrv;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.jndi.JndiContext;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;
import ru.statjobs.loader.common.dao.RawDataStorageDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.url.UrlTypes;
import ru.statjobs.loader.dao.RawDataStorageDaoJmsImpl;
import ru.statjobs.loader.utils.JsonUtils;

import static org.mockito.Mockito.*;


public class AppIT {

    static BrokerService broker = new BrokerService();

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
    }

    @Test
    public void routeTest() throws Exception {

        RawDataStorageDaoProxy postgresDao = mock(RawDataStorageDaoProxy.class);
        jndiContext.bind("postgresDao", postgresDao);
        camelContext.addComponent("mq", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false"));
        new App().createRoute(camelContext, App.RAW_QUEUE_NAME);
        camelContext.start();

        ProducerTemplate template = camelContext.createProducerTemplate();
        template.sendBody("mq:queue:" + App.RAW_QUEUE_NAME, "{\"link\":{\"url\":\"url123\",\"sequenceNum\":1,\"handlerName\":\"HH_RESUME\",\"props\":null},\"json\":\"{}\"}");
        template.sendBody("mq:queue:" + App.RAW_QUEUE_NAME, "{\"link\":{\"url\":\"url123\",\"sequenceNum\":1,\"handlerName\":\"HH_RESUME\",\"props\":null},\"json\":\"{}\"}");

        Thread.sleep(100);
        verify(postgresDao, times(2)).save(anyObject());
    }

    @Test
    public void failUrlTypeTest() throws Exception {
        RawDataStorageDao postgresMock = mock(RawDataStorageDao.class);
        RawDataStorageDaoProxy proxy = spy(new RawDataStorageDaoProxy(postgresMock));
        jndiContext.bind("postgresDao", proxy);
        camelContext.addComponent("mq", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false"));
        new App().createRoute(camelContext, App.RAW_QUEUE_NAME);
        camelContext.start();

        ProducerTemplate template = camelContext.createProducerTemplate();

        ListAppender.clear();

        template.sendBody("mq:queue:" + App.RAW_QUEUE_NAME, "{\"link\":{\"url\":\"url123\",\"sequenceNum\":1,\"handlerName\":\"HH_LIST_RESUME\",\"props\":null},\"json\":\"{}\"}");

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
        camelContext.addComponent("mq", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false"));
        new App().createRoute(camelContext, App.RAW_QUEUE_NAME);
        camelContext.start();

        ProducerTemplate template = camelContext.createProducerTemplate();

        ListAppender.clear();

        String invalidJson =  "{\"link\":{\"url\":\"url123\",\"sequenceNum\":1,\"handlerName\":\"sdfgsdfgsdfgdsfgsdfg\",\"props\":null},\"json\":\"{}\"}";

        template.sendBody("mq:queue:" + App.RAW_QUEUE_NAME, invalidJson);

        Thread.sleep(100);
        verify(postgresMock, times(0)).saveHhResume(anyObject(), anyString());
        verify(postgresMock, times(0)).saveHhVacancy(anyObject(), anyString());
        verify(proxy, times(0)).save(anyObject());
        Thread.sleep(100);

        Assert.assertTrue(StringUtils.join(ListAppender.getLogMap().get("ERROR"), ";").contains(invalidJson));
        Assert.assertTrue(StringUtils.join(ListAppender.getLogMap().get("ERROR"), ";").contains("InvalidFormatException"));

    }

    @Test
    public void appWithDaoJmsTest() throws Exception {
        RawDataStorageDaoJmsImpl daoJms = new RawDataStorageDaoJmsImpl(
                new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false"),
                App.RAW_QUEUE_NAME,
                new JsonUtils()
        );

        RawDataStorageDao postgresMock = mock(RawDataStorageDao.class);
        RawDataStorageDaoProxy proxy = spy(new RawDataStorageDaoProxy(postgresMock));
        jndiContext.bind("postgresDao", proxy);
        camelContext.addComponent("mq", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false"));
        new App().createRoute(camelContext, App.RAW_QUEUE_NAME);
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
