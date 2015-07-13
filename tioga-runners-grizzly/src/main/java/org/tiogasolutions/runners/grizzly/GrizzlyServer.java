package org.tiogasolutions.runners.grizzly;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.Application;
import java.io.IOException;import java.io.InputStream;import java.io.OutputStream;import java.lang.Exception;import java.lang.InterruptedException;import java.lang.Runnable;import java.lang.Runtime;import java.lang.String;import java.lang.StringBuilder;import java.lang.System;import java.lang.Thread;import java.lang.Throwable;
import java.net.ConnectException;import java.net.ServerSocket;import java.net.Socket;import java.net.SocketTimeoutException;import java.net.URI;import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.*;

public class GrizzlyServer {

  private static final int socketAcceptTimeoutMilli = 5000;

  private final GrizzlyServerConfig config;
  private final LoggerFacade logger;
  private final ResourceConfig resourceConfig;

  private HttpServer httpServer;
  private ServerSocket socket;
  private Thread acceptThread;
  
  /** handlerLock is used to synchronize access to socket, acceptThread and callExecutor. */
  private final ReentrantLock handlerLock = new ReentrantLock();

  public GrizzlyServer(Application application, GrizzlyServerConfig config, LoggerFacade logger) {
    this.config = config;
    this.logger = logger;

    if (application instanceof ResourceConfig) {
      resourceConfig = (ResourceConfig)application;
    } else {
      resourceConfig = new ResourceConfigAdapter(application);
    }
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
    return config.getBaseUri();
  }

  /**
   * The server's current configuration.
   * @return the property's value.
   */
  public GrizzlyServerConfig getConfig() {
    return config;
  }

  /** Starts the server. */
  public void start() {
    try {
      doStart(resourceConfig);

      logger.info(format("Application started at %s", getBaseUri()));
      logger.info(format("WADL available at %sapplication.wadl", getBaseUri()));

      if (config.isToOpenBrowser()) {
        URI baseUri = getBaseUri();
        java.awt.Desktop.getDesktop().browse(baseUri);
      }

      Thread.currentThread().join();

    } catch (Throwable e) {
      logger.error("Exception starting server", e);
      e.printStackTrace();
    }
  }

  protected void doStart(ResourceConfig resourceConfig) throws Exception {
    shutdownRemote(config.getHostName(), config.getShutdownPort());

    httpServer = GrizzlyHttpServerFactory.createHttpServer(config.getBaseUri(), resourceConfig);

    // Lock the handler, IllegalStateException thrown if we fail.
    lockHandler();
    try {
      if (acceptThread != null) {
        throw new java.lang.IllegalStateException("Socket handler thread is already running.");
      }

      try {
        // Set the accept timeout so we won't block indefinitely.
        socket = new ServerSocket(config.getShutdownPort());
        socket.setSoTimeout(socketAcceptTimeoutMilli);

        String msg = format("%s is accepting connections on port %s from %s.", getClass().getSimpleName(), config.getPort(), socket.getInetAddress().getHostAddress());
        logger.info(msg);

      } catch(IOException ex) {
        String msg = format("IOException starting server socket, maybe port %s was not available.", config.getPort());
        logger.error(msg, ex);
      }

      Thread shutdownThread = new Thread(httpServer::shutdown, "shutdownHook");
      Runtime.getRuntime().addShutdownHook(shutdownThread);

      Runnable acceptRun = GrizzlyServer.this::socketAcceptLoop;
      acceptThread = new Thread(acceptRun);
      acceptThread.start();

    } finally {
      // Be sure to always give up the lock.
      unlockHandler();
    }
  }

  /**
   * Shuts down *this* currently running Grizzly server.
   */
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

  private void lockHandler() throws TimeoutException, InterruptedException {
    int timeout = 5;
    TimeUnit timeUnit = TimeUnit.SECONDS;

    if (!handlerLock.tryLock(timeout, timeUnit)) {
      String msg = format("Failed to obtain lock within %s %s", timeout, timeUnit);
      throw new TimeoutException(msg);
    }
  }

  /**
   * Really just used to improve readability and so we limit when we directly access handlerLock.
   */
  private void unlockHandler() {
    handlerLock.unlock();
  }

  private void socketAcceptLoop() {

    // Socket accept loop.
    while (!Thread.interrupted()) {
      try {

        // REVIEW - Sleep to allow another thread to lock the handler (never seems to happen without this). Could allow acceptThread to be interrupted in stop without the lock.
        Thread.sleep(5);

        // Lock the handler so we don't accept a new connection while stopping.
        lockHandler();
        Socket client;

        // Ensure we have not stopped or been interrupted.
        if (acceptThread == null || Thread.interrupted()) {
          logger.info("Looks like SocketHandler has been stopped, terminate our acceptLoop.");
          System.out.println("Looks like SocketHandler has been stopped, terminate our acceptLoop.");
          return;
        }

        // We have are not stopped, so accept another connection.
        client = socket.accept();

        int val;
        StringBuilder builder = new StringBuilder();
        InputStream is = client.getInputStream();

        while ((val = is.read()) != -1) {
          builder.append((char)val);
          if ("SHUTDOWN".equals(builder.toString())) {
            logger.info("Shutdown command received.");
            System.out.println("Shutdown command received.");
            httpServer.shutdownNow();
            System.exit(0);
          }
        }

      } catch (SocketTimeoutException | TimeoutException ex) {
        // Accept timed out, which is excepted, try again.

      } catch (Throwable ex) {
        logger.error("Unexpected exception", ex);
        System.out.println("Unexpected exception");
        ex.printStackTrace();
        return;

      } finally {
        unlockHandler();
      }
    }
  }
}
