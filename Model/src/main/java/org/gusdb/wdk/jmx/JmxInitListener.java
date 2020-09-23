package org.gusdb.wdk.jmx;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.logging.MDCUtil;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;

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
    MDCUtil.setNonRequestThreadVars("jmxi");
    ServletContext context = sce.getServletContext();

    // Check whether model is initialized; if not, then initialization probably
    //   failed, so warn in logs and skip JMX initialization.  If this is caused
    //   by a race condition between the two listeners, will need to address in
    //   a different way.
    WdkModel model = (WdkModel)context.getAttribute(Utilities.WDK_MODEL_KEY);
    if (model == null) {
      LOG.warn("Missing model in ServletContext.  Skipping MBean registration.");
      return;
    }

    ContextThreadLocal.set(context);
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
    MDCUtil.setNonRequestThreadVars("jmxt");
    if (_registration != null) {
      _registration.destroy();
    }
  }

}