package ru.statjobs.loader.linksrv;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import ru.statjobs.loader.Consts;
import ru.statjobs.loader.utils.PropertiesUtils;

import javax.servlet.DispatcherType;
import java.net.URL;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Scanner;


public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static final String REDIS_HOST = "REDIS_HOST";
    public static final String REDIS_PORT = "REDIS_PORT";
    public static final String AUTH = "AUTH";
    public static final int SEVER_PORT = 8080;
    public static final int REDIS_DEFAULT_PORT = 6379;

    public static void main(String[] args) throws Exception {

        PropertiesUtils propertiesUtils = new PropertiesUtils();
        Properties props;
        if (args.length > 0) {
            props = propertiesUtils.loadPropertiesFromFile(args[0]);
            LOGGER.info("Properties file name: {}", args[0]);
        } else {
            props = propertiesUtils.loadProperties(Consts.PROPERTIES_FILE);
            LOGGER.info("Properties file name do not set. Default properties is loaded");
        }

        Server server = createServer(
                (String)props.get("redis"),
                REDIS_DEFAULT_PORT,
                (String)props.get("linksrvkey")
        );

        /* wait pres q */
        Scanner sc = new Scanner(System.in);
        while (true) {
            if (sc.hasNext()) {
                if (sc.next().toLowerCase().contains("q")) {
                    server.stop();
                    break;
                }
            }
        }
        LOGGER.info("Linksrv is stopped");
    }

    public static Server createServer(String redisHost, int redisPort, String seqKey) {
        Server server;
        try {
            WebAppContext webapp = createContext();
            webapp.getServletContext().setAttribute(REDIS_HOST, redisHost);
            webapp.getServletContext().setAttribute(REDIS_PORT, redisPort);
            webapp.getServletContext().setAttribute(AUTH, seqKey);
            LOGGER.info("Redis host: {}", redisHost);
            LOGGER.info("Start Server port {}", SEVER_PORT);
            server = new Server(SEVER_PORT);
            server.setHandler(webapp);
            server.start();
            return server;
        } catch (Exception e) {
            LOGGER.error("Server fail", e);
            throw new RuntimeException(e);
        }
    }


    public static WebAppContext createContext() {
        WebAppContext webapp = new WebAppContext();
        AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();

        ServletHolder servletHolder = new ServletHolder("dispatcher", new DispatcherServlet(webContext));
        servletHolder.setAsyncSupported(true);
        servletHolder.setInitOrder(1);
        webapp.addServlet(servletHolder, "/");
        webapp.addFilter(AuthFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        webapp.setInitParameter("contextConfigLocation", LinkSrvMvcConf.class.getName());
        webapp.addEventListener(new ContextLoaderListener(webContext));
        URL location = App.class.getProtectionDomain().getCodeSource().getLocation();
        webapp.setWar(location.toExternalForm());
        return webapp;
    }


}
