package ru.statjobs.loader.dao;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.statjobs.loader.common.dao.RawDataStorageDao;
import ru.statjobs.loader.common.dto.DownloadableLink;

import javax.jms.*;
import java.util.HashMap;

public class RawDataStorageDaoJmsImplIT {

    static BrokerService broker = new BrokerService();
    static ConnectionFactory factory;

    private final static String JSON = "{\"aa123\":\"aa321\"}";
    private final static String QUEUE_NAME = "tq";

    @BeforeClass
    public static void start() throws Exception {
        broker.addConnector("tcp://localhost:61616");
        broker.start();
        broker.waitUntilStarted();
        factory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
    }

    @AfterClass
    public static void stop() throws Exception {
        broker.stop();
        broker.waitUntilStopped();
    }

    @Test
    public void saveHhVacancySimpleTest() throws InterruptedException, JMSException {
        //
        RawDataStorageDao rawDataStorageDao = new RawDataStorageDaoJmsImpl(factory,QUEUE_NAME);
        rawDataStorageDao.saveHhVacancy(
                new DownloadableLink(
                        "url1",
                        123,
                        "handler1",
                        new HashMap<String, String>() {{
                            put("props1", "props11");
                            put("props2", "props22");

                }}),
                JSON
        );
        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer =session.createConsumer(session.createQueue(QUEUE_NAME));
        TextMessage message = (TextMessage) consumer.receive(1000);
        Assert.assertNotNull(message);
        Assert.assertNull(consumer.receive(100));
        String msg = message.getText();

        Assert.assertTrue(msg.contains("\"url\":\"url1\""));
        Assert.assertTrue(msg.contains("\"sequenceNum\":123"));
        Assert.assertTrue(msg.contains("\"handlerName\":\"handler1\""));
        Assert.assertTrue(msg.contains("\"props1\":\"props11\""));
        Assert.assertTrue(msg.contains("\"props2\":\"props22\""));
        Assert.assertTrue(msg.contains("aa123"));
        Assert.assertTrue(msg.contains("aa321"));

        consumer.close();
        session.close();
        connection.close();


    }

    @Test
    public void failConnection() {
        RawDataStorageDao rawDataStorageDao = new RawDataStorageDaoJmsImpl(new ActiveMQConnectionFactory("localhost123"), QUEUE_NAME);
        try {
            rawDataStorageDao.saveHhVacancy(new DownloadableLink("", 0, "", null), "");
        } catch (Exception e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void failConnectionInProcess() throws Exception {
        RawDataStorageDao rawDataStorageDao = new RawDataStorageDaoJmsImpl(factory, QUEUE_NAME);
        rawDataStorageDao.saveHhVacancy(new DownloadableLink("1", 0, "1", null), JSON);
        rawDataStorageDao.saveHhVacancy(new DownloadableLink("1", 0, "1", null), JSON);
        broker.stop();
        broker.waitUntilStopped();
        broker.start();
        broker.waitUntilStarted();

        rawDataStorageDao.saveHhVacancy(new DownloadableLink("failConnectionInProcess", 0, "1", null), JSON);

        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer =session.createConsumer(session.createQueue(QUEUE_NAME));
        TextMessage message = (TextMessage) consumer.receive(1000);
        Assert.assertNotNull(message);
        Assert.assertNull(consumer.receive(100));
        Assert.assertTrue(message.getText().contains("failConnectionInProcess"));
        consumer.close();
        session.close();
        connection.close();
    }


}
