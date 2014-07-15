package org.gusdb.wdk.jmx;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;

/**
 * An implementation of the ServletContextListener interface that receives 
 * notifications to register and unregister MBeans.
 * 
 * The listener should be defined in web.xml to be active. e.g.
 * <pre>
 * {@code
 * <listener>
 *   <listener-class>org.gusdb.wdk.jmx.JmxInitListener</listener-class>
 * </listener>
 * }
 * </pre>
 */
public final class JmxInitListener implements ServletContextListener {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(JmxInitListener.class);
  MBeanRegistration registration;
  
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ContextThreadLocal.set(sce.getServletContext());
    registration = new MBeanRegistration();
    registration.init();
    ContextThreadLocal.unset();

  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    registration.destroy();
  }

}