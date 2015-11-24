package org.tiogasolutions.runners.grizzly;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

public abstract class ShutdownUtils {

  /**
   * Attempts to shutdown a Grizzly server running on with the specified hostName and shutdownPort.
   * @param config the server config which defines the respective host name and shutdown port.
   * @throws IOException upon failure.
   */
  public static void shutdownRemote(GrizzlyServerConfig config) throws IOException {
    sendShutdown(config);
    waitForShutdown(config);
  }

  public static void sendShutdown(GrizzlyServerConfig config) throws IOException {
    try(Socket localSocket = new Socket(config.getHostName(), config.getShutdownPort())) {
      try(OutputStream outStream = localSocket.getOutputStream()) {
        outStream.write("SHUTDOWN".getBytes());
        outStream.flush();
      }
    } catch (ConnectException ignored) {/* ignored */}
  }

  public static void waitForShutdown(GrizzlyServerConfig config) throws IOException {
    long start = System.currentTimeMillis();
    do {
      if (isConnected(config) == false) return;
    } while (System.currentTimeMillis() - start < config.getShutdownTimeout());
  }

  public static boolean isConnected(GrizzlyServerConfig config) throws IOException {

    Socket socket = null;

    try {
      socket = new Socket(config.getHostName(), config.getShutdownPort());
      return true;

    } catch (ConnectException ignored) {
      return false;

    } finally {
      if (socket != null) {
        socket.close();
      }
    }
  }
}
