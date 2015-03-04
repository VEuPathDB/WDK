package org.gusdb.wdk.jmx;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Read java property file and return map of MBean object names and classes to load. Format is of the form
 * classname=MBeanName. e.g.
 *  
 * org.gusdb.wdk.jmx.mbeans.UserDB=org.apidb.wdk:group=Databases,type=UserDB
 */
public class MBeanSet {

  private static final Logger LOG = Logger.getLogger(MBeanSet.class);

  private static final String PROPERTIES_FILE = "mbeanset.properties";

  private static Map<String, String> MBEAN_MAPPING = null;

  /**
   * Reads default property file (mbeanset.properties) from classpath and
   * returns map of mbean class and mbean object name.  Format of property file is:
   * 
   * <p>org.gusdb.wdk.jmx.mbeans.UserDB=org.apidb.wdk:group=Databases,type=UserDB</p>
   *
   * @return  Map of mbean classes and object names to register
   */
  public static synchronized Map<String, String> getMbeanClassMapping() {
    if (MBEAN_MAPPING == null) {
      MBEAN_MAPPING = loadMbeanClassMapping(PROPERTIES_FILE);
    }
    return MBEAN_MAPPING;
  }


  public static Map<String, String> loadMbeanClassMapping(String propsFilePath) {

    Map<String, String> map = new HashMap<String, String>();

    Properties props = new Properties();
    try {
      props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propsFilePath));
    }
    catch (IllegalArgumentException | IOException ioe) {
      LOG.error("Error loading MBean props from " + propsFilePath, ioe);
    }
    catch (NullPointerException npe) {
      LOG.warn(propsFilePath + " not found in classpath. No MBeans will be loaded.");
    }

    @SuppressWarnings("unchecked")
    Enumeration<String> keys = (Enumeration<String>)props.propertyNames();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      map.put(key, props.getProperty(key));
    }

    return Collections.unmodifiableMap(map);
  }
}
