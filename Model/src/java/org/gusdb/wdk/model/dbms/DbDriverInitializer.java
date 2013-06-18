package org.gusdb.wdk.model.dbms;

import java.util.Properties;

/**
 * Provides interface so users can specify a custom database driver initializer.
 * 
 * @author rdoherty
 */
public interface DbDriverInitializer {

  /**
   * This method should initialize the driver named by <code>driverClassName</code>.
   * It is to be connected to by the <code>connectionUrl</code> using the properties
   * specified in <code>props</code>.  This method may modify the properties as
   * needed by a proxy driver or set of drivers, and can also modify the connection
   * URL that will be used to connect by returning a new URL.
   * 
   * @param driverClassName class name of driver to be initialized
   * @param connectionUrl URL that will be used to connect to this database
   * @param props initial connection properties
   * @return modified URL to be used to connect instead of the passed URL
   * @throws ClassNotFoundException
   */
  public String initializeDriver(String driverClassName, String connectionUrl, Properties props) throws ClassNotFoundException;

}
