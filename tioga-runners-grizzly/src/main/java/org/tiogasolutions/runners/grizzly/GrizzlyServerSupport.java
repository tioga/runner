package org.tiogasolutions.runners.grizzly;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;

import static java.lang.String.format;

public abstract class GrizzlyServerSupport {

  private static final Logger log = LoggerFactory.getLogger(GrizzlyServerSupport.class);

  protected HttpServer httpServer;

  protected final ResourceConfig resourceConfig;
  protected final GrizzlyServerConfig serverConfig;

  public GrizzlyServerSupport(GrizzlyServerConfig serverConfig, Application application) {
    this.serverConfig = serverConfig;
    this.resourceConfig = ResourceConfig.forApplication(application);
  }

  /**
   * The Jersey specific implementation of the JAX-RS application provided at
   * instantiation. If a ResourceConfig was specified at instantiation, then
   * that instance will be returned. If an instanceof Application was specified
   * at construction, then it is wrapped in an instance of ResourceConfigAdapter.
   * @return the property's value.
   */
  public ResourceConfig getResourceConfig() {
    return resourceConfig;
  }

  /**
   * Convenience method for getConfig().getBaseUri();
   * @return the property's value.
   */
  public URI getBaseUri() {
    return serverConfig.getBaseUri();
  }

  /**
   * The server's current configuration.
   * @return the property's value.
   */
  public GrizzlyServerConfig getConfig() {
    return serverConfig;
  }

  /** Starts the server. */
  public void start() {
    try {
      // If it's running, shut it down.
      shutdownRemote(serverConfig.getHostName(), serverConfig.getShutdownPort());

      // Create a new instance of our server.
      httpServer = GrizzlyHttpServerFactory.createHttpServer(serverConfig.getBaseUri(), resourceConfig);

      // Start our own shutdown handler.
      createShutdownHandler().start(httpServer);

      log.info(format("Application started at %s", getBaseUri()));
      log.info(format("WADL available at %sapplication.wadl", getBaseUri()));

      if (serverConfig.isToOpenBrowser()) {
        URI baseUri = getBaseUri();
        java.awt.Desktop.getDesktop().browse(baseUri);
      }

      Thread.currentThread().join();

    } catch (Throwable e) {
      log.error("Exception starting server", e);
      e.printStackTrace();
    }
  }

  protected ShutdownHandler createShutdownHandler() {
    return new ShutdownHandler(serverConfig);
  }

  /** Shuts down *this* currently running Grizzly server. */
  public void shutdownThis() {
    if (httpServer != null) {
      httpServer.shutdown();
    }
  }

  /**
   * Attempts to shutdown a Grizzly server running on with the specified hostName and shutdownPort.
   * @param hostName the host name this server is running at.
   * @param shutdownPort the shutdown port the server is listening to.
   * @throws IOException upon failure.
   */
  public static void shutdownRemote(String hostName, int shutdownPort) throws IOException {
    try(Socket localSocket = new Socket(hostName, shutdownPort)) {
      try(OutputStream outStream = localSocket.getOutputStream()) {
        outStream.write("SHUTDOWN".getBytes());
        outStream.flush();
      }
    } catch (ConnectException ignored) {
    }
  }

  public void register(Class type) {
    resourceConfig.register(type);
  }

  public void packages(String...packages) {
    resourceConfig.packages(packages);
  }
}