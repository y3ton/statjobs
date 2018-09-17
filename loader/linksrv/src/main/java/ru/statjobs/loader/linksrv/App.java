package ru.statjobs.loader.linksrv;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.DispatcherType;
import java.net.URL;
import java.util.EnumSet;
import java.util.Scanner;


public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static final String REDIS_HOST = "REDIS_HOST";
    public static final String REDIS_PORT = "REDIS_PORT";
    public static final String AUTH = "AUTH";
    public static final int SEVER_PORT = 8080;


    public static void main(String[] args) throws Exception {
        Server server = createServer("192.168.1.105", 6379, "");

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
