package ru.statjobs.loader.linksrv;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.net.URL;
import java.util.Scanner;


public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static final String REDIS_HOST = "REDIS_HOST";
    public static final String REDIS_PORT = "REDIS_PORT";
    public static final int SEVER_PORT = 8080;


    public static void main(String[] args) throws Exception {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                process("192.168.1.105", 6379);
            }
        });
        thread.start();

        /* wait pres q */
        Scanner sc = new Scanner(System.in);
        while (true) {
            if (sc.hasNext()) {
                if (sc.next().toLowerCase().contains("q")) {
                    thread.interrupt();
                    break;
                }
            }
        }
    }

    public static void process(String redisHost, int redisPort) {
        try {
            WebAppContext webapp = createContext();
            webapp.getServletContext().setAttribute(REDIS_HOST, redisHost);
            webapp.getServletContext().setAttribute(REDIS_PORT, redisPort);
            LOGGER.info("Redis host: {}", redisHost);
            LOGGER.info("Start Server port {}", SEVER_PORT);
            Server server = new Server(SEVER_PORT);
            server.setHandler(webapp);
            server.start();
            try {
                server.join();
            } catch (InterruptedException e) {
                LOGGER.info("Server interupted");
            }
            server.stop();
        } catch (Exception e) {
            LOGGER.error("Server fail", e);
        }
    }


    public static WebAppContext createContext() {
        WebAppContext webapp = new WebAppContext();
        AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();

        ServletHolder servletHolder = new ServletHolder("dispatcher", new DispatcherServlet(webContext));
        servletHolder.setAsyncSupported(true);
        servletHolder.setInitOrder(1);
        webapp.addServlet(servletHolder, "/");

        webapp.setInitParameter("contextConfigLocation", LinkSrvMvcConf.class.getName());
        webapp.addEventListener(new ContextLoaderListener(webContext));
        URL location = App.class.getProtectionDomain().getCodeSource().getLocation();
        webapp.setWar(location.toExternalForm());
        return webapp;
    }


}
