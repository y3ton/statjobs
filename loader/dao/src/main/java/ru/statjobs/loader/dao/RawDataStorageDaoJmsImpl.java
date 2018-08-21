package ru.statjobs.loader.dao;

import javax.jms.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.common.dao.RawDataStorageDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.dto.RawData;
import ru.statjobs.loader.utils.JsonUtils;

public class RawDataStorageDaoJmsImpl implements RawDataStorageDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawDataStorageDaoJmsImpl.class);
    private static final int NEXT_TRY_TIMEOUT = 100;

    private final ConnectionFactory connectionFactory;
    private final String queueName;
    private final JsonUtils jsonUtils;



    private Connection connection = null;
    private Session session = null;
    private Queue queue = null;
    private MessageProducer producer = null;

    public RawDataStorageDaoJmsImpl(ConnectionFactory connectionFactory, String queueName, JsonUtils jsonUtils) {
        this.connectionFactory = connectionFactory;
        this.queueName = queueName;
        this.jsonUtils = jsonUtils;
    }

    @Override
    public void saveHhVacancy(DownloadableLink link, String json) {
        save(link, json);
    }

    @Override
    public void saveHhResume(DownloadableLink link, String json) {
        save(link, json);
    }

    private boolean init() {
        try {
            if (connection == null) {
                connection  = connectionFactory.createConnection();
                connection.start();
                LOGGER.info("create JMS connection");
            }
            if (session == null) {
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                LOGGER.info("create JMS session");
            }
            if (queue == null) {
                queue = session.createQueue(queueName);
                LOGGER.info("create JMS queue");
            }
            if (producer == null) {
                producer = session.createProducer(queue);
                LOGGER.info("create JMS producer");
            }
        } catch (JMSException e) {
            LOGGER.error("fail create connection", e);
            close();
            return false;
        }
        return true;
    }

    public void close() {
        if (producer != null) {
            try {
                producer.close();
            } catch (JMSException e) {
                LOGGER.error("fail close producer");
            }
        }
        if (session != null) {
            try {
                session.close();
            } catch (JMSException e) {
                LOGGER.error("fail close session");
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                LOGGER.error("fail close connection");
            }
        }
        producer = null;
        queue = null;
        session = null;
        connection = null;
    }

    private void save(DownloadableLink link, String json) {
        if (!init()) {
            throw new RuntimeException("fail create connection");
        }
        try {
            send(link, json);
            return;
        } catch (Exception e) {
            LOGGER.error("fail send message {} {}", link.toString(), json);
        }
        try {
            Thread.sleep(NEXT_TRY_TIMEOUT);
        } catch (InterruptedException e) {
            LOGGER.error("Interupt wait", e);
        }
        close();
        init();


        try {
            send(link, json);
            return;
        } catch (Exception e) {
            LOGGER.error("fail send message {} {}", link.toString(), json);
            throw new RuntimeException(e);
        }
    }

    private void send(DownloadableLink link, String json) throws JMSException, JsonProcessingException {
        RawData rawData = new RawData(link, json);
        TextMessage message = session.createTextMessage(jsonUtils.createString(rawData));
        producer.send(message);
    }
}
