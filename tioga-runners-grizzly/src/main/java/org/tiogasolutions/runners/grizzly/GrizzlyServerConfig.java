package org.tiogasolutions.runners.grizzly;

import java.lang.String;import java.net.URI;

public class GrizzlyServerConfig {

  private String hostName = "localhost";
  private boolean shutDown = false;
  private int port = 8080;
  private int shutdownPort = 8005;
  private String context;
  private boolean toOpenBrowser;
  private long shutdownTimeout = 10*1000;

  /**
   * Identifies the host name for the server.
   * @return the property's value.
   */
  public String getHostName() {
    return hostName;
  }

  public GrizzlyServerConfig setHostName(String hostName) {
    this.hostName = hostName;
    return this;
  }

  /**
   * Identifies the port number for the server.
   * @return the property's value.
   */
  public int getPort() {
    return port;
  }

  public GrizzlyServerConfig setPort(int port) {
    this.port = port;
    return this;
  }

  /**
   * Identifies the shutdown port the server shall listen to for
   * a shutdown command (when starting) or the port number that a shutdown command
   * will be sent to (when shutting down).
   * @return the property's value.
   */
  public int getShutdownPort() {
    return shutdownPort;
  }

  public GrizzlyServerConfig setShutdownPort(int shutdownPort) {
    this.shutdownPort = shutdownPort;
    return this;
  }

  /**
   * Indicates if the server should be shut down. When set to true,
   * a new instance will not be started but rather a shutdown signal
   * will be sent to the server currently running on the specified
   * hostName and shutdownPort.
   * @return the property's value.
   */
  public boolean isShutDown() {
    return shutDown;
  }

  public GrizzlyServerConfig setShutDown(boolean shutDown) {
    this.shutDown = shutDown;
    return this;
  }

  /**
   * Identifies the application context (sometimes referred to as the
   * servlet context) that the application will be started in.
   * @return the property's value.
   */
  public String getContext() {
    return context;
  }

  public GrizzlyServerConfig setContext(String context) {
    this.context = context;
    return this;
  }

  /**
   * Indicates if a web browser should be opened after server start.
   * Typically used during testing and development.
   * @return the property's value.
   */
  public boolean isToOpenBrowser() {
    return toOpenBrowser;
  }

  public GrizzlyServerConfig setToOpenBrowser(boolean toOpenBrowser) {
    this.toOpenBrowser = toOpenBrowser;
    return this;
  }

  /**
   * Identifies the URI that the server will be running at given the host name,
   * port number and context.
   * @return the property's value.
   */
  public URI getBaseUri() {
    if (context == null || context.trim().isEmpty()) {
      return URI.create("http://"+ hostName +":"+ port+"/");
    } else {
      return URI.create("http://"+ hostName +":"+ port+"/"+context+"/");
    }
  }

  /**
   * Identifies the number of milliseconds to wait for the server to shut down.
   * @return the property's value.
   */
  public long getShutdownTimeout() {
    return shutdownTimeout;
  }

  public void setShutdownTimeout(long shutdownTimeout) {
    this.shutdownTimeout = shutdownTimeout;
  }
}
