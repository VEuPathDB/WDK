package org.gusdb.wdk.jmx.mbeans;

public interface ConnectionPoolMBean  {

  public String getPoolOwnerClassname();

  /* provided by connection pool library */
  public int getNumActive();
  public int getNumIdle();
  public int getMinIdle();
  public int getMaxIdle();
  public long getMinEvictableIdleTimeMillis();
  public long getTimeBetweenEvictionRunsMillis();
  public boolean getTestOnBorrow();
  public boolean getTestOnReturn();
  public boolean getTestWhileIdle();

  /* provided by application */
  public int getBorrowedCount();
  public int getReturnedCount();
  public int getCurrentlyOpenCount();
  public String getUnclosedConnectionInfo();
  
  public void dumpOpenDBConnections();
  public String showOpenDBConnections();
}
