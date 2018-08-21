package ru.statjobs.loader.loadsrv;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.util.jndi.JndiContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.common.dto.RawData;
import ru.statjobs.loader.dao.RawDataStorageDaoPostgresImpl;
import ru.statjobs.loader.utils.PropertiesUtils;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class App {

    public static final String PROPERTIES_FILE = "app.properties";
    public static final String RAW_QUEUE_NAME = "raw_queue";

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        Properties properties = new PropertiesUtils().loadProperties(PROPERTIES_FILE);
        JndiContext jndiContext = null;
        Connection connection = null;
        CamelContext camelContext = null;

        try {
            connection = DriverManager.getConnection(
                    properties.getProperty("url"),
                    properties.getProperty("user"),
                    properties.getProperty("password"));

            RawDataStorageDaoProxy postgresDao = new RawDataStorageDaoProxy(new RawDataStorageDaoPostgresImpl(connection));

            jndiContext = new JndiContext();
            jndiContext.bind("postgresDao", postgresDao);

            camelContext = new DefaultCamelContext(jndiContext);
            camelContext.addComponent("mq", ActiveMQComponent.activeMQComponent(properties.getProperty("rawMessageBrokerUrl")));

            /*start process*/
            new App().createRoute(camelContext, RAW_QUEUE_NAME);
            camelContext.start();

            System.in.read();
        } catch(Exception ex) {
            LOGGER.error("fail start app", ex);
        } finally {
            if (camelContext != null) {
                try {
                    camelContext.stop();
                } catch (Exception e) {
                    LOGGER.error("fail close camel context", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("fail close connection", e);
                }
            }
            if (jndiContext != null) {
                try {
                    jndiContext.close();
                } catch (NamingException e) {
                    LOGGER.error("fail close jndi", e);
                }
            }
        }
    }

    public void createRoute(CamelContext camelContext, String queue) throws Exception {
            camelContext.addRoutes(new RouteBuilder() {
                public void configure() {
                from("mq:queue:" + queue)
                    .routeId("a")
                    .doTry()
                        .unmarshal().json(JsonLibrary.Jackson, RawData.class)
                        .to("bean:postgresDao?method=save")
                        //.to("stream:out")
                    .endDoTry()
                    .doCatch(Exception.class)
                        .to("log:ru.statjobs.loader.loadsrv.App?level=ERROR&multiline=true&showBody=true&showStackTrace=true&showCaughtException=true")
                    .end();
                }
            });
    }


}
