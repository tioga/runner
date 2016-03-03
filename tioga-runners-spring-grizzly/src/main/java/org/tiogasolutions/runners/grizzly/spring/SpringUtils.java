package org.tiogasolutions.runners.grizzly.spring;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class SpringUtils {

  public static AbstractXmlApplicationContext createXmlConfigApplicationContext(String activeProfiles, String xmlConfigPath) {

    AbstractXmlApplicationContext applicationContext;

    if (xmlConfigPath.startsWith("classpath:")) {
      applicationContext = new ClassPathXmlApplicationContext();
    } else {
      applicationContext = new FileSystemXmlApplicationContext();
    }

    applicationContext.setConfigLocation(xmlConfigPath);
    applicationContext.getEnvironment().setActiveProfiles(split(activeProfiles));
    applicationContext.refresh();
    return applicationContext;
  }

  public static AnnotationConfigApplicationContext createAnnotationConfigApplicationContext(String activeProfiles, Class<?>[] annotatedClasses) {
    AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
    applicationContext.getEnvironment().setActiveProfiles(split(activeProfiles));
    applicationContext.register(annotatedClasses);
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
}
