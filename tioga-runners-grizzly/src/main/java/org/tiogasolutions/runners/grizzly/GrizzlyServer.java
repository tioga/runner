package org.tiogasolutions.runners.grizzly;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiogasolutions.app.common.App;
import org.tiogasolutions.app.common.status.AppInfo;
import org.tiogasolutions.app.common.status.AppStatus;
import org.tiogasolutions.app.common.status.ChangeAppStatus;
import org.tiogasolutions.app.common.status.ChangeAppTestUser;

import javax.ws.rs.core.Application;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class GrizzlyServer implements App {

  private static final Logger log = LoggerFactory.getLogger(GrizzlyServer.class);

  protected AppInfo appInfo;
  protected HttpServer httpServer;

  protected final ResourceConfig resourceConfig;
  protected final GrizzlyServerConfig serverConfig;

  public GrizzlyServer(GrizzlyServerConfig serverConfig, Application application) {
    this.serverConfig = serverConfig;
    this.resourceConfig = ResourceConfig.forApplication(application);
    this.appInfo = new AppInfo(AppStatus.UNKNOWN, "Not Started.", null, null);
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

  public HttpServer getHttpServer() {
    return httpServer;
  }

  public GrizzlyServerConfig getServerConfig() {
    return serverConfig;
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
      execute(new ChangeAppStatus(AppStatus.STARTING, "Not Started."));

      // If it's running, shut it down.
      ShutdownUtils.shutdownRemote(serverConfig);

      // Create a new instance of our server.
      httpServer = GrizzlyHttpServerFactory.createHttpServer(serverConfig.getBaseUri(), resourceConfig);

      log.info("Application started at {}", getBaseUri());
      log.info("WADL available at {}application.wadl", getBaseUri());

      // Start our own shutdown handler.
      createShutdownHandler().start(httpServer);

      if (serverConfig.isToOpenBrowser()) {
        log.info("Opening web browser to {}", getBaseUri());
        URI baseUri = getBaseUri();
        java.awt.Desktop.getDesktop().browse(baseUri);
      }

      execute(new ChangeAppStatus(AppStatus.ENABLED, "Application enabled."));

      Thread.currentThread().join();

    } catch (Throwable e) {
      log.error("Exception starting server", e);
      e.printStackTrace();
    }
  }

  protected ShutdownHandler createShutdownHandler() {
    return new ShutdownHandler(this, serverConfig);
  }

  /** Shuts down *this* currently running Grizzly server. */
  @Override
  public AppInfo shutdown() {
    execute(new ChangeAppStatus(AppStatus.RESTRICTED, "Shutting down gracefully (30 seconds max)"));
    if (httpServer != null) {
      httpServer.shutdown(30, TimeUnit.SECONDS);
    }
    return execute(new ChangeAppStatus(AppStatus.DISABLED, "Offline"));
  }

  public void register(Class type) {
    resourceConfig.register(type);
  }

  public void packages(String...packages) {
    resourceConfig.packages(packages);
  }

  @Override
  public AppInfo getAppInfo() {
    return appInfo;
  }

  @Override
  public AppInfo execute(ChangeAppStatus change) {
    return appInfo = new AppInfo(
        change.getStatus(),
        change.getMessage(),
        appInfo.getTestEmailAddress(),
        appInfo.getTestPassword()
    );
  }

  @Override
  public AppInfo execute(ChangeAppTestUser change) {
    return appInfo = new AppInfo(
        appInfo.getStatus(),
        appInfo.getMessage(),
        change.getTestEmailAddress(),
        change.getTestPassword()
    );
  }
}
