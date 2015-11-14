package org.tiogasolutions.runners.grizzlyspring;

import org.springframework.beans.factory.BeanFactory;
import org.tiogasolutions.runners.grizzly.GrizzlyServerConfig;

public interface ServerConfigResolver {

  /**
   * Gets a GrizzlyServerConfig, potentially from the specified BeanFactory
   * @param factory the bean factory from which the server configuration may be retrieved.
   * @return the grizzly server config.
   */
  public GrizzlyServerConfig getConfig(BeanFactory factory);

  /**
   * Creates an instances that extracts an instance of GrizzlyServerConfig from the specified bean factory by name.
   * @param beanName the bean name of the GrizzlyServerConfig to be extracted from the bean factory.
   * @return the server config resolver.
   */
  public static ServerConfigResolver fromBeanName(final String beanName) {
    return new ServerConfigResolver() {
      @Override
      public GrizzlyServerConfig getConfig(BeanFactory factory) {
        return factory.getBean(beanName, GrizzlyServerConfig.class);
      }
    };
  }

  /**
   * Creates an instances that extracts an instance of GrizzlyServerConfig from the specified bean factory by application class.
   * @param grizzlyServerConfigClass the class of the specific GrizzlyServerConfig to be extracted from the bean factory.
   * @return the server config resolver.
   */
  public static ServerConfigResolver fromClassName(final Class<? extends GrizzlyServerConfig> grizzlyServerConfigClass) {
    return new ServerConfigResolver() {
      @Override
      public GrizzlyServerConfig getConfig(BeanFactory factory) {
        return factory.getBean(grizzlyServerConfigClass);
      }
    };
  }

  /**
   * Simply returns the specified GrizzlyServerConfig.
   * @param grizzlyServerConfig the actual instance of GrizzlyServerConfig to be used.
   * @return the server config resolver.
   */
  public static ServerConfigResolver fromApplication(final GrizzlyServerConfig grizzlyServerConfig) {
    return new ServerConfigResolver() {
      @Override
      public GrizzlyServerConfig getConfig(BeanFactory factory) {
        return grizzlyServerConfig;
      }
    };
  }
}
