package org.fhir;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;

/**
 * Setup Jetty http servlet
 * **/

public class FhirServer {
    private final Server server;

    public FhirServer(int port) {
        server = new Server(port);

        ServletContextHandler ctx = new ServletContextHandler();

        ctx.setContextPath("/");
        ctx.addServlet(
                new ServletHolder(new HelloServlet()),
                "/print"
        );

        //Register Fhir server
        server.setHandler(ctx);
    }

    public void start() throws Exception{
        System.out.println("Starting Jetty...");
        server.start();
        System.out.println("Jetty started successfully!");
        System.out.println("Server URI: " + server.getURI());

        //server.join();
    }
}
