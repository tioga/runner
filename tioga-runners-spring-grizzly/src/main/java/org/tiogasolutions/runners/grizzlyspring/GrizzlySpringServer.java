package org.tiogasolutions.runners.grizzlyspring;

import org.glassfish.jersey.server.spring.SpringLifecycleListener;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.tiogasolutions.runners.grizzly.GrizzlyServerSupport;

public class GrizzlySpringServer extends GrizzlyServerSupport {

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

    this(serverConfigResolver, applicationResolver, createAnnotationConfigApplicationContext(activeProfiles, annotatedClasses));
  }

  /**
   * Creates an instance with a GenericXmlApplicationContext Spring context.
   * @param serverConfigResolver utility to resolve an instance of the GrizzlyServerConfig class.
   * @param applicationResolver utility to resolve an instance of the Application class.
   * @param activeProfiles a comma separated list of Spring profiles to be activated.
   * @param resources a list of Resources by which to initialize the GenericXmlApplicationContext.
   */
  public GrizzlySpringServer(ServerConfigResolver serverConfigResolver,
                             ApplicationResolver applicationResolver,
                             String activeProfiles,
                             Resource...resources) {

    this(serverConfigResolver, applicationResolver, createGenericXmlApplicationContext(activeProfiles, resources));
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

    resourceConfig.register(SpringLifecycleListener.class);
    resourceConfig.register(RequestContextFilter.class);
    resourceConfig.property("contextConfig", applicationContext);
  }

  public static AnnotationConfigApplicationContext createAnnotationConfigApplicationContext(String activeProfiles, Class<?>[] annotatedClasses) {
    AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
    applicationContext.getEnvironment().setActiveProfiles(split(activeProfiles));
    applicationContext.register(annotatedClasses);
    applicationContext.refresh();
    return applicationContext;
  }

  public static GenericXmlApplicationContext createGenericXmlApplicationContext(String activeProfiles, Resource[] resources) {
    GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext(resources);
    applicationContext.getEnvironment().setActiveProfiles(split(activeProfiles));
    applicationContext.refresh();
    return applicationContext;
  }

  private static String[] split(String activeProfiles) {
    String[] values = (activeProfiles == null) ? new String[0] : activeProfiles.split(",");
    for (int i = 0; i < values.length; i++) {
      values[i] = values[i].trim();
    }
    return values;
  }

  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }
}
