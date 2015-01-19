package org.gusdb.wdk.jmx;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.MapBuilder;

/**
 * Registers mbeans named in MBeanSet .
 */
public class MBeanRegistration {

  private static final Logger LOG = Logger.getLogger(MBeanRegistration.class);

  private MBeanServer _server = ManagementFactory.getPlatformMBeanServer();
  private List<ObjectName> _registeredMBeans = new ArrayList<ObjectName>();

  /** 
   * Initialization of MBean server and registration of MBeans.
   */
  public void init() {
    registerMBeans();
  }

  /** 
   * Unregister mbeans and cleanup
   */
  public void destroy() {
    unregisterMBeans();
  }
  
  /**
   * Return the virtual hostname defined for the Tomcat context.
   * This hostname is used for MBean object naming
   */
  private String getHostName() {
    // temp method until getHostNameBROKEN_IN_TC5() can be fixed
    return "localhost";
  }
  
  /** 
   * Unused method that will replace getHostname() .
   * Tomcat 5 throws
   * java.lang.ClassNotFoundException: org.apache.catalina.core.StandardContext
   * with the following code. This class is in $CATALINA_BASE/server/lib/catalina.jar
   * 
   * The host lookup works in TC 6.0.33
   * The hostname is hardcoded in getHostName() until Tomcat 5 support
   * is dropped.
   * @throws JmxInitException 
   */
  @SuppressWarnings("unused")
  private String getHostNameBROKEN_IN_TC5() throws HostnameResolutionException {

    /**
     * I can't find a direct way to get the Host we are deploying into. I did find
     * this technique in
     * fr.xebia.management.ServletContextAwareMBeanServerFactory
     */
    ServletContext servletContext = ContextThreadLocal.get();

    try {
      Field standardContextHostNameField = getClass("org.apache.catalina.core.StandardContext").getDeclaredField("hostName");
      standardContextHostNameField.setAccessible(true);

      Field applicationContextFacadeContextField = getClass("org.apache.catalina.core.ApplicationContextFacade").getDeclaredField("context");
      applicationContextFacadeContextField.setAccessible(true);
      
      Field applicationContextContextField = getClass("org.apache.catalina.core.ApplicationContext").getDeclaredField("context");
      applicationContextContextField.setAccessible(true);

      Object applicationContext = applicationContextFacadeContextField.get(servletContext);
      Object standardContext = applicationContextContextField.get(applicationContext);
      return (String) standardContextHostNameField.get(standardContext);
    }
    catch (IllegalArgumentException | IllegalAccessException |
        NoSuchFieldException | SecurityException | ClassNotFoundException e) {
      throw new HostnameResolutionException("Unable to load hostname from Tomcat", e);
    }
  }
  
  /**
   * Do registration of set of MBeans in server.
   *
   * @see MBeanSet
   */
  private void registerMBeans() {
    for (Entry<String, String> entry : MBeanSet.getMbeanClassMapping().entrySet()) {
      try {
        // get a set of named MBeans using config name and object from the entry;
        //   object might be an MBean or MBean factory; if factory, name may be parameterized
        Map<String, Object> namedMbeans = getNamedMbeans(
            entry.getValue(), getInstanceOfClass(entry.getKey()));

        // create object name and register each generated MBean
        for (Entry<String, Object> mbean : namedMbeans.entrySet()) {
          ObjectName objName = makeObjectName(mbean.getKey());
          LOG.debug("registering mbean " + objName.toString());
          _server.registerMBean(mbean.getValue(), objName);
          _registeredMBeans.add(objName);
        }
      }
      catch (ClassNotFoundException | MBeanRegistrationException |
          InstanceAlreadyExistsException | NotCompliantMBeanException |
          MalformedObjectNameException | InstantiationException |
          IllegalAccessException | ContextResolutionException e) {
        LOG.error("Unable to load and register MBean entry { " + entry.getKey() + ", " + entry.getValue() + " }", e);
      }
    }
  }

  private Map<String, Object> getNamedMbeans(String name, Object object) {
    if (object instanceof NamedMBeanFactory) {
      // caller provided a class to generate some number of MBeans
      NamedMBeanFactory factory = (NamedMBeanFactory)object;
      // pass in parameterized name to factory to get map of created beans
      return factory.getNamedMbeans(name);
    }
    else {
      // assume user provided a class to be directly registered
      return new MapBuilder<String, Object>().put(name, object).toMap();
    }
  }

  /**
   * Unregistration set of MBeans in server.
   *
   * @see MBeanSet
   */
  private void unregisterMBeans() {
    for (ObjectName name : _registeredMBeans) {
      try {
        LOG.debug("Unregistering MBean " + name.toString());
        _server.unregisterMBean(name);
      }
      catch (InstanceNotFoundException | MBeanRegistrationException e) {
        LOG.warn("Exception while unregistering MBean " + name + " " + e);
      }
    }
  }

  /**
   * Return class instance for given class name
   *
   * @param className name of class to instantiate
   * @return instance of the named class
   * @throws ClassNotFoundException
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  private Object getInstanceOfClass(String className)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    return getClass(className).newInstance();
  }

  /**
   * Returns the Class object associated with the class or interface with the given string name
   *
   * @param className name of class
   * @return Class object for the given name
   * @throws ClassNotFoundException if unable to find class on classpath
   */
  private Class<?> getClass(String className) throws ClassNotFoundException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    return Class.forName(className, false, loader);
  }

  /**
   * Construct an MBean object name from a precursor name.
   * 
   * @param precursor object name
   * @return MBean ObjectName
   * @throws MalformedObjectNameException 
   * @throws ContextResolutionException 
   */
  private ObjectName makeObjectName(String pObjectNameString)
      throws MalformedObjectNameException, ContextResolutionException { 
    String path = getContextPath();
    String host = getHostName();
    String objectNameString = pObjectNameString + ",path=//" + host + path;
    return new ObjectName(objectNameString);
  }

  /**
   * Return the application context path
   *
   * @return context path
   * @throws ContextResolutionException 
   */
  private String getContextPath() throws ContextResolutionException {
    ServletContext sc = ContextThreadLocal.get();
    String contextName = null;

    if (sc.getMajorVersion() > 2 || sc.getMajorVersion() == 2 && sc.getMinorVersion() >= 5) {
      // Servlet API is >= 2.5 and has ServletContext getContextPath() method but 
      // we have to make an indiret method call so code compiles for API < 2.5
      Method m;
      try {
        m = sc.getClass().getMethod("getContextPath", new Class[] {});
        contextName = (String) m.invoke(sc, (Object[]) null);
      }
      catch (NoSuchMethodException | SecurityException | IllegalAccessException |
          IllegalArgumentException | InvocationTargetException e) {
        throw new ContextResolutionException("Unable to resolve context path", e);
      }
    }
    else {
      // hack for old servlet API: use the name of the tempdir
      String tmpdir = ((java.io.File) sc.getAttribute("javax.servlet.context.tempdir")).getName();
      contextName = "/" + tmpdir;
    }

    return contextName;
  }
  
  @SuppressWarnings("serial")
  private static class ContextResolutionException extends Exception {
    public ContextResolutionException(String message, Exception cause) {
      super(message, cause);
    }
  }

  @SuppressWarnings("serial")
  private static class HostnameResolutionException extends Exception {
    public HostnameResolutionException(String message, Exception cause) {
      super(message, cause);
    }
  }
}