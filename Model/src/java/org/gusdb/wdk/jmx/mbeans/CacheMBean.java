package org.gusdb.wdk.jmx.mbeans;

/**
 * MBean representing the WDK database cache.
 */
public interface CacheMBean {
  public String getcache_table_count();
  public void resetWdkCache();
}
