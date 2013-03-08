/**
 * 
 */
package org.gusdb.wdk.model.dbms;


import org.apache.log4j.Logger;

/**
 * @author Steve Fischer
 * Allow logging of example queries to a separate logger.
 * 
 */
public final class SqlExampleLog {

  static final Logger logger = Logger.getLogger(SqlExampleLog.class);


  /**
   * private constructor, make sure SqlExampleLog cannot be instanced.
   */
  private SqlExampleLog() {}

}
