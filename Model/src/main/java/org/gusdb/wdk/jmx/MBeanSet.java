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

  private static final Logger logger = Logger.getLogger(MBeanSet.class);
  private static final String mbeansetProperty = "mbeanset.properties";

  /**
   * Reads property file from classpath and
   * returns map of mbean class and mbean object name. 
   * Format of property file is
   *     org.gusdb.wdk.jmx.mbeans.UserDB=org.apidb.wdk:group=Databases,type=UserDB
   *
   * @return  Map of mbean classes and object names to register
   */
  public static Map<String, String> getMbeanClassMapping() {

    Map<String, String> map = new HashMap<String, String>();

    Properties props = new Properties();
    try {
        props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(mbeansetProperty));
    } catch (IOException ioe) {
        logger.error(ioe);
    } catch (NullPointerException npe) {
        logger.warn(mbeansetProperty + " not found in classpath. No MBeans will be loaded.");
    }

    Enumeration<?> e = props.propertyNames();
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      map.put(key, props.getProperty(key));
    }

    return Collections.unmodifiableMap(map);
  
  }

}
