package org.tiogasolutions.runners.grizzly;

import org.glassfish.grizzly.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;

public class ShutdownHandler {

  private static final Logger log = LoggerFactory.getLogger(GrizzlyServer.class);

  private static final int socketAcceptTimeoutMilli = 5000;

  private ServerSocket socket;
  private Thread acceptThread;
  private HttpServer httpServer;

  private final GrizzlyServerConfig config;

  /** handlerLock is used to synchronize access to socket, acceptThread and callExecutor. */
  private final ReentrantLock handlerLock = new ReentrantLock();

  public ShutdownHandler(GrizzlyServerConfig config) {
    this.config = config;
  }

  public void start(HttpServer httpServer) throws TimeoutException, InterruptedException {
    this.httpServer = httpServer;

    // Lock the handler, exception thrown if we fail.
    lockHandler();

    if (acceptThread != null) {
      throw new java.lang.IllegalStateException("Socket handler thread is already running.");
    }

    try {
      // Set the accept timeout so we won't block indefinitely.
      socket = new ServerSocket(config.getShutdownPort());
      socket.setSoTimeout(socketAcceptTimeoutMilli);

      try {
        Thread shutdownThread = new Thread(httpServer::shutdown, "shutdownHook");
        Runtime.getRuntime().addShutdownHook(shutdownThread);

        acceptThread = new Thread(this::socketAcceptLoop);
        acceptThread.start();

        String msg = format("%s is accepting connections on port %s from %s.", getClass().getSimpleName(), config.getShutdownPort(), socket.getInetAddress().getHostAddress());
        log.info(msg);

      } finally {
        // Be sure to always give up the lock.
        unlockHandler();
      }

    } catch(IOException ex) {
      String msg = format("IOException starting server socket, maybe port %s was not available.", config.getPort());
      log.error(msg, ex);
    }
  }

  protected void socketAcceptLoop() {

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
          log.info("Looks like SocketHandler has been stopped, terminate our acceptLoop.");
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
            log.info("Shutdown command received.");
            System.out.println("Shutdown command received.");
            httpServer.shutdownNow();
            System.exit(0);
          }
        }

      } catch (SocketTimeoutException | TimeoutException ex) {
        // Accept timed out, which is excepted, try again.

      } catch (Throwable ex) {
        log.error("Unexpected exception", ex);
        System.out.println("Unexpected exception");
        ex.printStackTrace();
        return;

      } finally {
        unlockHandler();
      }
    }
  }

  /**
   * Really just used to improve readability and so we limit when we directly access handlerLock.
   */
  protected void unlockHandler() {
    handlerLock.unlock();
  }

  protected void lockHandler() throws TimeoutException, InterruptedException {
    int timeout = 5;
    TimeUnit timeUnit = TimeUnit.SECONDS;

    if (!handlerLock.tryLock(timeout, timeUnit)) {
      String msg = format("Failed to obtain lock within %s %s", timeout, timeUnit);
      throw new TimeoutException(msg);
    }
  }
}
