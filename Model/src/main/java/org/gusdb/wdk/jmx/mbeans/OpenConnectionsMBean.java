package org.gusdb.wdk.jmx.mbeans;

public interface OpenConnectionsMBean {

  public void dumpOpenUserDBConnections();
  public String showOpenUserDBConnections();
  public String getOpenUserDBConnections();

  public void dumpOpenAppDBConnections();
  public String showOpenAppDBConnections();
  public String getOpenAppDBConnections();

}
