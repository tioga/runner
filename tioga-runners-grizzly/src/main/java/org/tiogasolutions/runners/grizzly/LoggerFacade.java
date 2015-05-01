package org.tiogasolutions.runners.grizzly;

public interface LoggerFacade {
  void info(String message);
  void warn(String message);
  void error(String message, Throwable e);
}
