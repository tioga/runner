package org.tiogasolutions.runners.grizzly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;

public class GrizzlyServer extends GrizzlyServerSupport {

  private static final Logger log = LoggerFactory.getLogger(GrizzlyServer.class);

  public GrizzlyServer(GrizzlyServerConfig serverConfig, Application application) {
    super(serverConfig, application);
  }
}
