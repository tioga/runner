package org.tiogasolutions.runners.grizzlyspring;

import org.springframework.beans.factory.BeanFactory;

import javax.ws.rs.core.Application;

public interface ApplicationResolver {

  /**
   * Gets a JAX-RS Application, potentially from the specified BeanFactory.
   * @param factory the bean factory from which the JAX-RS application may be retrieved.
   * @return the application.
   */
  public Application getApplication(BeanFactory factory);

  /**
   * Creates an instances that extracts an instance of Application from the specified bean factory by name.
   * @param beanName the bean name of the Application to be extracted from the bean factory.
   * @return the application resolver.
   */
  public static ApplicationResolver fromBeanName(final String beanName) {
    return new ApplicationResolver() {
      @Override
      public Application getApplication(BeanFactory factory) {
        return factory.getBean(beanName, Application.class);
      }
    };
  }

  /**
   * Creates an instances that extracts an instance of Application from the specified bean factory by application class.
   * @param applicationClass the class of the specific Application to be extracted from the bean factory.
   * @return the application resolver.
   */
  public static ApplicationResolver fromApplicationClass(final Class<? extends Application> applicationClass) {
    return new ApplicationResolver() {
      @Override
      public Application getApplication(BeanFactory factory) {
        return factory.getBean(applicationClass);
      }
    };
  }

  /**
   * Simply returns the specified Application.
   * @param application the actual instance of Application to be used.
   * @return the application resolver.
   */
  public static ApplicationResolver fromApplication(final Application application) {
    return new ApplicationResolver() {
      @Override
      public Application getApplication(BeanFactory factory) {
        return application;
      }
    };
  }
}
