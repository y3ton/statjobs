package ru.statjobs.loader.linksrv;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.net.URL;



public class App {

    public static void main(String[] args) throws Exception {
        App app = new App();
        WebAppContext webapp = app.createContext();

        Server server = new Server(8081);
        server.setHandler(webapp);
        server.start();
        server.join();
    }

    WebAppContext createContext() {
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
