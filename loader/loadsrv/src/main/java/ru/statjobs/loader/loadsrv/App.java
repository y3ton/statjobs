package ru.statjobs.loader.loadsrv;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.util.jndi.JndiContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.Consts;
import ru.statjobs.loader.common.dto.RawData;
import ru.statjobs.loader.dao.RawDataStorageDaoPostgresImpl;
import ru.statjobs.loader.utils.PropertiesUtils;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;


public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        PropertiesUtils propertiesUtils = new PropertiesUtils();
        Properties props;
        if (args.length > 0) {
            props = propertiesUtils.loadPropertiesFromFile(args[0]);
            LOGGER.info("Properties file name: {}", args[0]);
        } else {
            props = propertiesUtils.loadProperties(Consts.PROPERTIES_FILE);
            LOGGER.info("Properties file name do not set. Default properties is loaded");
        }
        JndiContext jndiContext = null;
        Connection connection = null;
        CamelContext camelContext = null;

        LOGGER.info("DB url: {}", props.getProperty("url"));
        try {
            connection = DriverManager.getConnection(
                    props.getProperty("url"),
                    props.getProperty("user"),
                    props.getProperty("password"));

            RawDataStorageDaoProxy postgresDao = new RawDataStorageDaoProxy(new RawDataStorageDaoPostgresImpl(connection));

            jndiContext = new JndiContext();
            jndiContext.bind("postgresDao", postgresDao);

            camelContext = new DefaultCamelContext(jndiContext);
            // AWS SQS Connection
            SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
                    new ProviderConfiguration(),
                    AmazonSQSClientBuilder.standard()
                            .withRegion(Regions.US_WEST_2)
                            .withCredentials(new PropertiesFileCredentialsProvider(props.getProperty("awscredfile")))
            );
            LOGGER.info("JMS factory created: {}", connection.getClass().getName());
            camelContext.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

            /*start process*/
            new App().createRoute(camelContext, props, true);
            camelContext.start();

            /* wait pres q */
            Scanner sc = new Scanner(System.in);
            while (true) {
                if (sc.hasNext()) {
                    if (sc.next().toLowerCase().contains("q")) {
                        break;
                    }
                }
            }
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
        LOGGER.info("Load service stopped");
    }

    public void createRoute(CamelContext camelContext, Properties props, boolean fromJms) throws Exception {
            camelContext.addRoutes(new RouteBuilder() {
                public void configure() {
                    ((fromJms) ?
                            from("jms:" + Consts.RAW_QUEUE_NAME)
                            :
                            (
                                    from("jetty:" + "http://0.0.0.0:" +  Consts.ENDPOINT_PORT +  Consts.ENDPOINT_URL)
                                    .filter(header("Authorization").isEqualTo(props.getProperty("linksrvkey")))
                            )
                    )
                    .threads(1)
                    .routeId("a")
                    .doTry()
                        .unmarshal().json(JsonLibrary.Jackson, RawData.class)
                        //.to("stream:out")
                        .to("bean:postgresDao?method=save")
                    .endDoTry()
                    .doCatch(Exception.class)
                        .to("log:ru.statjobs.loader.loadsrv.App?level=ERROR&multiline=true&showBody=true&showStackTrace=true&showCaughtException=true")
                    .end();
                }
            });
    }


}
