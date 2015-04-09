package org.tiogasolutions.runner.jersey.support;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.springframework.beans.factory.ListableBeanFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JerseySpringBridge extends AbstractBinder {

  private final List<Object> nonSpringObjects = new ArrayList<>();
  private final ListableBeanFactory beanFactory;

  public JerseySpringBridge(ListableBeanFactory beanFactory, Object... nonSpringObjects) {
    this.beanFactory = beanFactory;
    Collections.addAll(this.nonSpringObjects, nonSpringObjects);
  }

  @Override
  protected void configure() {

    for (Object object : nonSpringObjects) {
      bindFactory(new Factory<Object>() {
        @Override public void dispose(Object instance) {}
        @Override public Object provide() {
          return object;
        }
      }).to(object.getClass());
    }

    String[] beanNames = beanFactory.getBeanDefinitionNames();
    for (String beanName : beanNames) {
      Object bean = beanFactory.getBean(beanName);

      bindFactory(new Factory<Object>() {
        @Override public void dispose(Object instance) {}
        @Override public Object provide() {
          return bean;
        }
      }).to(bean.getClass());
    }
  }
}
