package org.tiogasolutions.runners.grizzly.spring;

import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.springframework.context.ApplicationContext;
import org.tiogasolutions.runners.grizzly.GrizzlyServerConfig;
import org.tiogasolutions.runners.grizzly.GrizzlyServerSupport;
import org.tiogasolutions.runners.grizzly.ShutdownHandler;

public class GrizzlySpringServer extends GrizzlyServerSupport {

  public static final Class<?>[] GRIZZLY_CLASSES = new Class[]{
      GrizzlySpringServer.class, GrizzlyServerConfig.class, GrizzlyServerSupport.class, ShutdownHandler.class
  };

  private final ApplicationContext applicationContext;

  /**
   * Creates an instance with an AnnotationConfigApplicationContext Spring context.
   * @param serverConfigResolver utility to resolve an instance of the GrizzlyServerConfig class.
   * @param applicationResolver utility to resolve an instance of the Application class.
   * @param activeProfiles a comma separated list of Spring profiles to be activated.
   * @param annotatedClasses the list of annotated classes to be registered in the spring factory.
   */
  public GrizzlySpringServer(ServerConfigResolver serverConfigResolver,
                             ApplicationResolver applicationResolver,
                             String activeProfiles,
                             Class<?>...annotatedClasses) {

    this(serverConfigResolver, applicationResolver, SpringUtils.createAnnotationConfigApplicationContext(activeProfiles, annotatedClasses));
  }

  /**
   * Creates an instance with a GenericXmlApplicationContext Spring context.
   * @param serverConfigResolver utility to resolve an instance of the GrizzlyServerConfig class.
   * @param applicationResolver utility to resolve an instance of the Application class.
   * @param activeProfiles a comma separated list of Spring profiles to be activated.
   * @param xmlConfigPath the path to the Spring XML config file..
   */
  public GrizzlySpringServer(ServerConfigResolver serverConfigResolver,
                             ApplicationResolver applicationResolver,
                             String activeProfiles,
                             String xmlConfigPath) {

    this(serverConfigResolver, applicationResolver, SpringUtils.createXmlConfigApplicationContext(activeProfiles, xmlConfigPath));
  }

  /**
   * Creates an instance using the user-defined ApplicationContext.
   * @param serverConfigResolver utility to resolve an instance of the GrizzlyServerConfig class.
   * @param applicationResolver utility to resolve an instance of the ApplicationResolver class.
   * @param applicationContext the user-specified Spring application context.
   */
  public GrizzlySpringServer(ServerConfigResolver serverConfigResolver,
                             ApplicationResolver applicationResolver,
                             ApplicationContext applicationContext) {

    super(serverConfigResolver.getConfig(applicationContext),
          applicationResolver.getApplication(applicationContext));

    this.applicationContext = applicationContext;

    // resourceConfig.register(SpringLifecycleListener.class);
    resourceConfig.register(RequestContextFilter.class);
    resourceConfig.property("contextConfig", applicationContext);
  }

  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }
}
