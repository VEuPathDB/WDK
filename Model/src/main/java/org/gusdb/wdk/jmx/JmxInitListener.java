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

  private static final Logger LOG = Logger.getLogger(JmxInitListener.class);

  private MBeanRegistration _registration;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ContextThreadLocal.set(sce.getServletContext());
    try {
      _registration = new MBeanRegistration();
      _registration.init();
    }
    catch (Exception e) {
      LOG.error("Unable to complete WDK MBean registration", e);
    }
    finally {
      ContextThreadLocal.unset();
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    _registration.destroy();
  }

}